import importlib.util, pathlib, subprocess, tempfile, unittest
spec=importlib.util.spec_from_file_location('cloud_apply',pathlib.Path(__file__).resolve().parents[1]/'cloud-apply.py')
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class SyncTests(unittest.TestCase):
 def setUp(self):
  self.temp=tempfile.TemporaryDirectory();self.root=pathlib.Path(self.temp.name)
  self.remote=self.root/'remote.git';self.cloud=self.root/'cloud';self.business=self.root/'business'
  self.git(self.root,'init','--bare',str(self.remote))
  self.git(self.root,'init','-b','main',str(self.cloud))
  for repo in (self.cloud,self.business):
   if repo==self.business:self.git(self.root,'init','-b','feature/business',str(repo))
   self.git(repo,'config','commit.gpgsign','false');self.git(repo,'config','user.name','Sync test');self.git(repo,'config','user.email','sync-test@example.invalid')
  self.source='module/src/main/java/Thing.java';self.other='module/src/main/java/Other.java'
  for repo in (self.cloud,self.business):
   (repo/'module/src/main/java').mkdir(parents=True)
   (repo/self.source).write_text('class Thing {\n  int value = 1;\n}\n')
   (repo/self.other).write_text('class Other {\n  int value = 1;\n}\n')
   (repo/'pom.xml').write_text('local configuration\n' if repo==self.business else 'cloud configuration\n')
   self.git(repo,'add','.');self.git(repo,'commit','-m','baseline')
  self.git(self.cloud,'remote','add','origin',str(self.remote));self.git(self.cloud,'push','-u','origin','main')
  self.git(self.business,'remote','add','origin','https://gitlab.example.invalid/original.git')
  m.EXPECTED_REMOTE=str(self.remote);m.BASELINE_COMMIT=self.git(self.cloud,'rev-parse','HEAD').strip()
  self.key='branch.feature/business.cloudAppliedCommit'
 def tearDown(self):self.temp.cleanup()
 def git(self,repo,*args):
  p=subprocess.run(['git','-C',str(repo),*args],capture_output=True,text=True)
  if p.returncode:raise AssertionError(p.stderr)
  return p.stdout
 def update(self,both=False):
  (self.cloud/self.source).write_text('class Thing {\n  int value = 2;\n}\n')
  if both:(self.cloud/self.other).write_text('class Other {\n  int value = 2;\n}\n')
  (self.cloud/'pom.xml').write_text('cloud changed configuration\n')
  self.git(self.cloud,'add','.');self.git(self.cloud,'commit','-m','change');self.git(self.cloud,'push','origin','main')
 def test_apply_preserves_config_unrelated_edits_branch_and_remote(self):
  self.update();(self.business/'notes.txt').write_text('pending local work')
  branch=m.validate(self.cloud,self.business);m.apply_changes(self.cloud,self.business,branch)
  self.assertIn('value = 2',(self.business/self.source).read_text())
  self.assertEqual('local configuration\n',(self.business/'pom.xml').read_text())
  self.assertEqual('pending local work',(self.business/'notes.txt').read_text())
  self.assertEqual('feature/business',self.git(self.business,'branch','--show-current').strip())
  self.assertEqual('https://gitlab.example.invalid/original.git',self.git(self.business,'remote','get-url','origin').strip())
  self.assertEqual('',self.git(self.business,'diff','--cached','--name-only'))
  before=self.git(self.business,'status','--porcelain');m.apply_changes(self.cloud,self.business,branch)
  self.assertEqual(before,self.git(self.business,'status','--porcelain'))
 def test_conflict_is_atomic_and_does_not_advance_checkpoint(self):
  self.update(both=True);(self.business/self.other).write_text('class Other {\n  int value = 3;\n}\n')
  with self.assertRaises(RuntimeError):m.apply_changes(self.cloud,self.business,m.validate(self.cloud,self.business))
  self.assertIn('value = 1',(self.business/self.source).read_text())
  self.assertIn('value = 3',(self.business/self.other).read_text())
  self.assertNotEqual(0,subprocess.run(['git','-C',str(self.business),'config','--get',self.key],capture_output=True).returncode)
 def test_dry_run_does_not_modify_sources_or_checkpoint(self):
  self.update();m.apply_changes(self.cloud,self.business,m.validate(self.cloud,self.business),True)
  self.assertIn('value = 1',(self.business/self.source).read_text())
  self.assertNotEqual(0,subprocess.run(['git','-C',str(self.business),'config','--get',self.key],capture_output=True).returncode)
 def test_wrong_remote_is_rejected(self):
  self.git(self.cloud,'remote','set-url','origin','https://github.com/other/repo.git')
  with self.assertRaises(RuntimeError):m.validate(self.cloud,self.business)
 def test_symlink_is_rejected(self):
  self.update();p=self.business/self.source;p.unlink();p.symlink_to(self.business/self.other)
  with self.assertRaises(RuntimeError):m.apply_changes(self.cloud,self.business,m.validate(self.cloud,self.business))
 def test_alias_preserves_existing_configuration(self):
  self.git(self.business,'config','alias.cloud-fetch','fetch cloud')
  self.git(self.business,'config','alias.cloud-pull','!echo fixture')
  m.install_alias(self.cloud,self.business)
  self.assertIn('cloud-apply.py',self.git(self.business,'config','--get','alias.cloud-sync'))
  self.git(self.business,'config','alias.cloud-sync','!echo user alias')
  with self.assertRaises(RuntimeError):m.install_alias(self.cloud,self.business)
  self.assertEqual('!echo user alias',self.git(self.business,'config','--get','alias.cloud-sync').strip())
if __name__=='__main__':unittest.main()
