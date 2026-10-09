#!/usr/bin/env python3
"""Apply new cloud Java source changes to the original business checkout."""

import argparse
from pathlib import Path, PurePosixPath
import shlex
import subprocess
import sys

EXPECTED_REMOTE = "https://github.com/15841307074/sun.git"
BASELINE_COMMIT = "07181eeb97b1c59fa78016cd2ceb98fc3bb7cdca"


def git(repo, *args, data=None, optional=False):
    result = subprocess.run(
        ["git", "-c", f"safe.directory={repo}", "-C", str(repo), *args],
        input=data, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
    )
    if result.returncode and not optional:
        # Keep remote URLs, proxy details and source contents out of diagnostics.
        raise RuntimeError(f"Git {args[0]} failed; no sync checkpoint was advanced. "
                           "Inspect Git status locally for conflicts or access problems.")
    return result


def output(repo, *args):
    return git(repo, *args).stdout.decode("utf-8").strip()


def validate(cloud, business):
    for repo in (cloud, business):
        root = Path(output(repo, "rev-parse", "--show-toplevel")).resolve()
        if root != repo:
            raise RuntimeError("Each path must identify a checkout root.")
    if cloud == business:
        raise RuntimeError("The business and sanitized cloud checkouts must be different.")
    # Read configured identity without printing URLs which might contain credentials.
    remote = output(cloud, "config", "--get", "remote.origin.url")
    if remote != EXPECTED_REMOTE:
        raise RuntimeError("Cloud origin is not the expected sanitized GitHub repository.")
    if output(business, "config", "--get", "remote.origin.url") == EXPECTED_REMOTE:
        raise RuntimeError("Business checkout must retain its original remote.")
    branch = output(business, "symbolic-ref", "--short", "HEAD")
    for name in ("MERGE_HEAD", "CHERRY_PICK_HEAD", "REVERT_HEAD", "rebase-merge", "rebase-apply"):
        location = Path(output(business, "rev-parse", "--git-path", name))
        if not location.is_absolute():
            location = business / location
        if location.exists():
            raise RuntimeError("Finish the in-progress Git operation before syncing.")
    if git(business, "diff", "--name-only", "--diff-filter=U").stdout:
        raise RuntimeError("Resolve existing conflicts before syncing.")
    return branch


def install_alias(cloud, business):
    script = cloud / "tools" / "cloud-apply.py"
    command = "!git cloud-fetch && git cloud-pull && " + " ".join(
        shlex.quote(str(value).replace("\\", "/"))
        for value in (sys.executable, script, "--business-repo", business)
    )
    previous = git(business, "config", "--get", "alias.cloud-sync", optional=True)
    if previous.returncode == 0 and previous.stdout.decode().strip() != command:
        raise RuntimeError("An existing cloud-sync alias differs; it was preserved.")
    for alias in ("cloud-fetch", "cloud-pull"):
        if git(business, "config", "--get", f"alias.{alias}", optional=True).returncode:
            raise RuntimeError(f"Install the existing {alias} alias in the local task first.")
    git(business, "config", "alias.cloud-sync", command)
    print("Installed git cloud-sync in the original checkout only.")


def apply_changes(cloud, business, branch, dry_run=False):
    key = f"branch.{branch}.cloudAppliedCommit"
    saved = git(business, "config", "--get", key, optional=True)
    base = saved.stdout.decode().strip() if saved.returncode == 0 else BASELINE_COMMIT
    git(cloud, "fetch", "origin", "refs/heads/main:refs/remotes/origin/main")
    target = output(cloud, "rev-parse", "refs/remotes/origin/main^{commit}")
    if git(cloud, "merge-base", "--is-ancestor", base, target, optional=True).returncode:
        raise RuntimeError("The saved cloud checkpoint is not an ancestor; review history manually.")
    names = git(cloud, "diff", "--no-renames", "--name-only", "-z", base, target).stdout
    paths = [name.decode("utf-8") for name in names.split(b"\0") if name]
    sources = []
    for name in paths:
        relative = PurePosixPath(name)
        if relative.is_absolute() or ".." in relative.parts:
            raise RuntimeError("Unexpected path in cloud changes.")
        # Apply Java source changes only. Local POM/YAML, SQL, docs and helpers
        # remain unchanged and must be integrated separately when needed.
        if "src" not in relative.parts or relative.suffix != ".java":
            continue
        location = business
        for part in relative.parts:
            location = location / part
            if location.is_symlink():
                raise RuntimeError("A destination source path is a symlink; review it manually.")
        sources.append(name)
    if sources:
        patch = git(cloud, "diff", "--no-renames", "--binary", "--full-index",
                    base, target, "--", *sources).stdout
        # Neither command changes the index or commits. Git's default apply is
        # atomic: conflicting contexts stop the whole patch without --reject.
        git(business, "apply", "--check", data=patch)
        if not dry_run:
            git(business, "apply", data=patch)
    if not dry_run:
        git(business, "config", key, target)
    print(f"Cloud commit: {target[:12]}")
    print(f"{'Ready to apply' if dry_run else 'Applied'} Java source paths: {len(sources)}")
    for name in sources:
        print("  " + name)
    for name in paths:
        if name not in sources:
            print("Preserved local file; manual integration if needed: " + name)
    print("Review and commit changes in IDEA. Original configuration, branch and remote were preserved.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--business-repo", type=Path, required=True)
    parser.add_argument("--install-alias", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    cloud = Path(__file__).resolve().parent.parent
    business = args.business_repo.resolve()
    try:
        branch = validate(cloud, business)
        if args.install_alias:
            install_alias(cloud, business)
        else:
            apply_changes(cloud, business, branch, args.dry_run)
    except (RuntimeError, OSError) as error:
        print(str(error), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
