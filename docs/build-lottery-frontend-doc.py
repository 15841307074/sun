"""Build an offline frontend handoff from the checked Java contract."""
from pathlib import Path
import re, json, html, subprocess

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'youdao-module-promotion/youdao-module-promotion-biz/src/main/java/com/htyoudao/youdao/module/promotion'
OUT = ROOT / 'docs/lottery-frontend-swagger.html'
e = html.escape

def read(relative):
    return (JAVA / relative).read_text(encoding='utf-8-sig')

models = {}
def model(name, relative, new=()):
    text = read(relative)
    rows = []
    prev = 0
    for match in re.finditer(r'(?:private|public)\s+([\w<>., ]+)\s+(\w+)\s*(?:=[^;]*)?;', text):
        typ, field = match.group(1).strip(), match.group(2)
        if field == 'serialVersionUID': continue
        context = text[prev:match.start()]
        descriptions = re.findall(r'@Schema\([^)]*description\s*=\s*"([^"]*)"', context)
        comments = re.findall(r'/\*\*(.*?)\*/', context, re.S)
        description = descriptions[-1] if descriptions else (re.sub(r'\s*\*\s*', ' ', comments[-1]).strip() if comments else '沿用现有业务字段')
        rows.append(dict(name=field, type=typ, required='@NotNull' in context, desc=description, new=field in new))
        prev = match.end()
    models[name] = dict(rows=rows, source=relative)
    return name

model('LotteryVO', 'controller/app/lottery/vo/LotteryVO.java', ['requestId'])
model('LotteryUserLogVO', 'controller/app/lottery/vo/LotteryUserLogVO.java', ['requestId','grantStatus'])
model('LotterySettingsAddReqVO', 'controller/admin/lottery/vo/LotterySettingsAddReqVO.java', ['appScope','tagIds'])
model('LotterySettingsUpdateReqVO', 'controller/admin/lottery/vo/LotterySettingsUpdateReqVO.java', ['appScope','tagIds'])
model('LotterySettingsDetailDataRespVO', 'controller/admin/lottery/vo/LotterySettingsDetailDataRespVO.java', ['appScope','tagIds','tagInfoDTOS'])
model('LotterySettingsRespVO', 'controller/admin/lottery/vo/LotterySettingsRespVO.java', ['activityId','appScope','activityStore','tagIds','tagInfoDTOS','storeInfoDTOS','storeIds'])
model('LotterySettingsCacheDataVO', 'controller/admin/lottery/vo/LotterySettingsCacheDataVO.java', ['configVersion','stockEpoch','runtimeVersion','appScope','tagIds'])
model('LotterySettingsNumVo', 'controller/admin/lottery/vo/LotterySettingsNumVo.java')
model('LotteryPrizeReqVO', 'controller/admin/lottery/vo/LotteryPrizeReqVO.java')
model('LotteryTaskReqVO', 'controller/app/lottery/vo/LotteryTaskReqVO.java')
model('LotteryRecentWinnerRespVO', 'controller/app/lottery/vo/LotteryRecentWinnerRespVO.java', ['memberMobile','prizeName'])
model('LotteryLogVO', 'controller/app/lottery/vo/LotteryLogVO.java')
model('LotteryLogDO', 'dal/dataobject/lottery/LotteryLogDO.java', ['isGuarantees'])
model('LotteryTypeVO', 'controller/app/lottery/vo/LotteryTypeVO.java')
model('LotteryReissueReqVO', 'controller/admin/lottery/vo/LotteryReissueReqVO.java', ['lotteryId','memberId','storeId','requestId','reason'])
model('LotteryReissueRespVO', 'controller/admin/lottery/vo/LotteryReissueRespVO.java', ['queued','message','result'])
for fields_model in models.values():
    for row in fields_model['rows']:
        if row['name']=='lotteryType':
            row['desc']='抽奖布局类型：1 转盘、2 九宫格、3 福袋、4 盲盒；不是 prizeType 奖品类型'

sources = {
 'requestId': '前端为一次抽奖意图生成；建议 UUID，8–64 位字母/数字/下划线/短横线。超时、查询、同次重试始终复用，下一次明确的新抽奖才换新值。',
 'appScope': '后台表单的适用范围单选：0 门店，1 标签。详情/列表回显来自后端活动配置。',
 'tagIds': '后台标签选择器选中的标签值 ID；从现有 /system/tag/value/page 或 /system/tag/getTagList 获取，取条目的 id，不能取 tagGroupId。',
 'tagInfoDTOS': '后端根据已保存 tagIds 查询标签名称，供编辑页和列表展示；保存时仍提交 tagIds，不能只提交名称对象。',
 'grantStatus': '后端发奖/转账状态；可能包含 PENDING、PROCESSING、ACCEPTED、WAIT_USER_CONFIRM、TRANSFERING、CANCELING、SUCCESS、FAILED、RETURNED。',
 'configVersion': '后端维护的配置版本，只读；修改配置后可能变化。前端无需提交或递增。',
 'stockEpoch': '后端维护的库存批次，只读；切换批次/场次重置时可能变化。前端无需生成。',
 'runtimeVersion': '后端运行版本，只读：1 旧流程，2 新流程。小程序从详情 data.cacheData.runtimeVersion 读取；缺失/null 时按旧流程兼容。',
 'activityId': '活动主表 ID；来自后台列表行 activityId 或详情 data.activityId。不要与抽奖配置 id 混用。',
 'activityStore': '后台门店范围单选：1 全部门店，0 非全部。标签模式后端固定为 0；不表示抽奖次数或奖池模式。',
 'storeIds': '门店模式为选择器选中的门店 ID；标签模式回显的是后端同步后的适用门店，参加时后端仍实时校验标签。全部门店模式可能返回空数组。',
 'storeInfoDTOS': '后端当前适用门店展示数据。取 storeId/storeName 进行回显；不是保存依据，保存使用 storeIds。',
 'isGuarantees': '中奖时后端保存的兜底快照：0 非兜底，1 兜底，历史未核对可为 null。仅在直接返回中奖记录实体的接口中可见。',
 'memberMobile': '后端输出的脱敏手机号，如 159****5152；直接显示，不需要获取完整手机号。',
 'memberId': '小程序当前会员资料/登录态的会员 ID；不从最近中奖列表取得。',
 'storeId': '小程序当前选中的门店上下文；结果查询必须与原抽奖使用相同门店。',
 'lotteryId': '按接口区分活动引用；抽奖/详情/结果/最近中奖接口支持活动主表 ID 或配置 ID，建议统一使用活动主表 ID。',
 'packageInfo': '后端微信转账返回的确认凭据，可能为空或稍后才出现。仅在待用户确认且值存在时使用现有微信确认收款逻辑。',
 'outBillNo': '后端生成的商户转账单号；不是 requestId，也不是前端生成字段。',
 'code': '奖品稳定编码，已有字段。编辑保留详情返回值，不要每次编辑重新生成；新奖品不填时后端生成。',
 'remainNum': '已有字段；V2 后台详情奖品表示已占用数量（预占 + 已发放），不是可用库存。展示可用量需结合 prizeNum，兜底另按原规则。',
 'isEnabled': '后台活动启停控件，0 停用，1 启用；单独启停用 updateState，新增保存后默认停用。',
 'state': '抽奖配置启停，0 停用，1 启用；编辑提交时与 isEnabled 保持一致，不能把页面显示状态当库存状态。',
 'prizes': '后台奖品编辑表单；沿用 LotteryPrizeReqVO，每个奖池必须有且仅有一项 isGuarantees=1。',
 'prizeName': '最近中奖接口来自中奖记录的奖品名称；抽奖/结果接口来自本次选中或兜底后的奖品快照。',
 'cityName': '沿用页面定位/城市选择得到的城市名称，用于奖品城市限制；没有时可不传。',
 'isFree': '沿用客户端抽奖方式参数。V2 最终机会来源由后端选择，不能仅靠此字段强制免费。',
 'integral': '沿用客户端原参数；V2 积分消耗由后端配置 price 决定，不能以前端传值为扣费依据。',
 'reason': '后台人员填写人工补发原因，非空，最多 200 字；进入操作日志，不用于重新选择奖品。',
 'queued': '后端是否在本次调用安排补发任务；true 不代表已发放成功，false 时查看 message。',
 'message': '后端给出的安排结果说明，如已发放、不重复安排、正在执行。',
 'result': '原请求当前的 LotteryUserLogVO，沿用原奖品和请求 ID；发放进度看 grantStatus。',
}

def table(headers, rows):
    return '<div class="table-scroll"><table><thead><tr>'+''.join('<th>'+e(x)+'</th>' for x in headers)+'</tr></thead><tbody>'+''.join('<tr>'+''.join('<td>'+str(c)+'</td>' for c in row)+'</tr>' for row in rows)+'</tbody></table></div>'

def fields(name, only_new=False):
    rows=[]
    for r in models[name]['rows']:
        if only_new and not r['new']: continue
        desc = sources.get(r['name'], '后台活动/奖品表单填写或详情回显，沿用已有取值。' if 'ReqVO' in name else '后端返回，沿用已有展示字段。')
        required = '注解必填' if r['required'] else '见接口条件'
        rows.append([f'<code>{e(r["name"])}</code>' + (' <b class="new">新增</b>' if r['new'] else ''), e(r['type']), required, e(r['desc']), e(desc)])
    return table(['字段','Java 类型 / Long 建议字符串','必填条件','含义','字段来源 / 前端处理'],rows)

def code(value, label='JSON 示例'):
    text=json.dumps(value,ensure_ascii=False,indent=2) if not isinstance(value,str) else value
    return f'<div class="code-label">{e(label)} <button class="copy" type="button">复制</button></div><pre><code>{e(text)}</code></pre>'

def common(value): return dict(code=0,data=value,msg='')
draw=dict(lotteryId='2104867816196874242',memberId='1970000000000000001',storeId='1165829637767168',requestId='7cdf28a9-3710-4b1e-a08f-1207bfe1be3b')
accepted=dict(requestId=draw['requestId'],grantStatus='PENDING',lotteryPrizeId='2104867835138351106',prizeName='100积分',prizeType=2,prizeImgUrl='https://example.com/prize.png',packageInfo=None,outBillNo=None)
scope=dict(appScope=1,tagIds=['1962794800302530562'],activityStore=0)
admin_detail=dict(id='2104867827068510209',activityId=draw['lotteryId'],**scope,tagInfoDTOS=[dict(id='1962794800302530562',name='付款下单',remark=None,tagGroupId=None)],storeIds=[draw['storeId']],storeInfoDTOS=[dict(storeId=draw['storeId'],storeName='示例门店')],isEnabled=0,state=0)
apis=[]
def api(key,group,method,path,title,status,when,params,request,response,notes,models_used=()):
    apis.append(dict(key=key,group=group,method=method,path=path,title=title,status=status,when=when,params=params,request=request,response=response,notes=notes,models=models_used))

base='/app-api/promotion/lottery-mobile'
api('activity-entry','小程序','GET',base+'/getLotteryTypeList','当前门店的活动入口（沿用，V2 范围同步）','关联','加载小程序抽奖活动入口；切换门店时重新加载。',
 [('storeId','Long','建议必传','当前门店上下文；为空或非正数返回空数组')],None,common([dict(id=admin_detail['id'],activityId=draw['lotteryId'],lotteryType=1,lotteryTitle='活动展示标题',activityImgUrl='https://example.com/activity.png')]),
 ['条目 id 是抽奖配置 ID，activityId 是活动主表 ID；从此接口条目 activityId 传给抽奖页 lotteryId。','此接口的 lotteryType 是转盘/九宫格/福袋/盲盒布局类型，不是奖品类型。','活动入口可能来自旧流程或 V2，打开页面后继续读取详情 runtimeVersion。','列表缓存或标签同步存在短暂延迟时，进入详情及抽奖仍由后端校验实际门店资格。'],['LotteryTypeVO'])
api('result','小程序', 'POST',base+'/result','查询同一次抽奖及发奖结果','新增','提交超时或断网后恢复中奖结果；查询红包领取信息或异步发放进度；人工排查时查询原请求。',
 [('lotteryId','Long','必填','原抽奖的活动引用'),('memberId','Long','必填','原抽奖会员'),('storeId','Long','必填','原抽奖门店'),('requestId','String','必填','原抽奖 requestId，8–64 位指定字符')],draw,common(accepted),
 ['该接口只查询，不发起新抽奖、不重复扣次数，也不会触发补发。人工查询后需通过受控后台操作补发，不能反复调用 result 当作重发奖品。','未查到受理流水时 code=0，data=null；不能当作已失败/已中奖。网络结果不确定时稍后查，必要时以同一 requestId 重发原 lottery 请求。','HTTP/业务错误不会表示已受理流水被撤销。不要因为一次查询失败就生成新 requestId。','轮询建议 1–2 秒起步，逐渐退避至 3–5 秒；属前端建议，不是后端强制时间。页面离开取消定时器，保留请求上下文以便恢复。'],['LotteryUserLogVO'])
api('winners','小程序','GET',base+'/recent-winners','最新 20 条非兜底中奖记录','新增','进入抽奖页加载滚动中奖区；自己抽奖完成后刷新；展示区可低频刷新。',
 [('lotteryId','Long','必填','活动主表 ID，兼容配置 ID')],None,common([dict(memberMobile='159****5152',prizeName='100积分')]),
 ['无记录返回 data=[]；不足 20 条返回实际数量，不支持 pageNo/pageSize/limit。','汇总活动所有门店，按记录时间倒序；同一用户多次真实中奖可显示多条。','后端排除兜底、无奖品和未核对兜底快照的历史记录；红包完成发奖并保存记录后才展示。','每条只有 memberMobile、prizeName，不包含用户 ID、头像、中奖时间、门店或完整手机号。','展示接口失败可保留上次列表，不能阻断抽奖主流程。'],['LotteryRecentWinnerRespVO'])
api('draw','小程序','POST',base+'/lottery','发起抽奖（立即返回奖品，异步发放）','变更','用户明确点击一次抽奖时，先生成并保存本次 requestId，再调用一次。',
 [('lotteryId','Long','V2 必填','活动详情/路由上下文'),('memberId','Long','V2 必填','当前会员'),('storeId','Long','V2 必填','当前门店'),('requestId','String','V2 必填','前端生成，同次重试不变'),('cityName','String','可选','沿用城市定位'),('isFree / integral','Integer','沿用','不控制 V2 实际扣费/机会来源')],draw,common(accepted),
 ['code=0 且有奖品数据时，前端直接完成本轮中奖展示，不等待异步发奖；中奖不等同于奖励已到账。','同次请求固定活动、会员、门店与 requestId；下一次明确抽奖换新 ID。防重范围包含项目、活动、会员、requestId。','网络超时优先调用 result；不要重生成 requestId 盲目重抽。','grantStatus 仅说明发放进度，不作为转盘动画或是否允许下一次抽奖的条件；当前后台仍可能在红包失败后切换兜底，此次字段移除没有调整该发奖补偿逻辑。','旧活动继续走旧流程，新增 requestId/grantStatus 可为空；runtimeVersion=1/缺失时沿用原页面处理。'],['LotteryUserLogVO','LotteryVO'])
api('app-detail','小程序','POST',base+'/getLotteryDetailApp','活动详情（新增 cacheData 运行字段）','变更','进入抽奖页先请求，确定流程版本和当前门店是否可参加。',
 [('lotteryId','Long','必填','活动主表 ID，兼容配置 ID'),('storeId','Long','V2 必填','当前门店'),('memberId','Long','建议沿用','当前会员上下文')],{k:v for k,v in draw.items() if k!='requestId'},common(dict(cacheData=dict(id=admin_detail['id'],activityId=draw['lotteryId'],appScope=1,tagIds=scope['tagIds'],runtimeVersion=2,configVersion=3,stockEpoch=1),prizes=[])),
 ['新增字段在 data.cacheData 内，不在 data 顶层。详情校验活动状态和门店范围，失败时不继续开放抽奖按钮。','runtimeVersion/configVersion/stockEpoch 是后端字段，不属于后台新增/编辑提交字段。','标签是否命中由后端判断，前端不按 tagIds 自己计算参加资格。'],['LotterySettingsCacheDataVO'])
api('quota','小程序','POST',base+'/lotteryNum','刷新可用次数（结构沿用，V2 语义统一）','关联','进入活动页、抽奖受理/完成后、完成任务后、重新进入页面时。',
 [('lotteryId','Long','必填','当前活动'),('memberId','Long','必填','当前会员'),('storeId','Long','建议沿用','当前门店上下文')],{k:v for k,v in draw.items() if k!='requestId'},common(dict(num=2,sum=5,freeAvailableCount=1,pointsAvailableCount=1,orderAvailableCount=0,shareAvailableCount=0,browseAvailableCount=0,orderFinishCount=0,shareFinishCount=0,browseFinishCount=0)),
 ['这些次数字段是已有字段，不是本轮新增。V2 num 为各机会来源合计并受总/周期上限约束，sum 为活动个人总剩余次数。','num/sum 或来源次数为 -1 表示不限制，不能直接渲染成负数或当作零次数。','最终是否可抽以后端受理结果为准；前端不手动替代服务器累加。'],['LotterySettingsNumVo'])
api('verify','小程序','POST',base+'/verifyLottery','点击前资格提示（沿用）','关联','可沿用当前页面的抽奖前校验，校验后再调用 lottery。',
 [('lotteryId','Long','必填','活动引用'),('memberId','Long','必填','当前会员'),('storeId','Long','必填','当前门店')],{k:v for k,v in draw.items() if k!='requestId'},None,
 ['不是预约/锁库存接口。校验通过不保证后续抽奖一定成功；lottery 会重新校验。','响应 data 沿用原接口，前端依据 code/msg 展示提示。'])
api('records','小程序','POST',base+'/getLotteryLogByMemberId','会员中奖记录（增加兜底快照）','变更','查看我的奖品/中奖记录；抽奖完成后按原逻辑刷新。',
 [('lotteryId','Long','沿用','依 LotteryLogVO'),('memberId','Long','沿用','当前会员，非中奖列表用户')],dict(lotteryId=draw['lotteryId'],memberId=draw['memberId']),None,
 ['该接口直接返回中奖记录实体列表，条目增加 isGuarantees。','isGuarantees 为抽奖时的兜底标记；历史 null 不应擅自当作非兜底。','其他字段和领奖流程沿用原接口；最近中奖展示请使用 recent-winners。'],['LotteryLogVO'])

base='/admin-api/promotion/lottery-log'
api('admin-list','管理后台','GET',base+'/getLotterySettingsListPage','活动列表（新增范围回显字段）','变更','活动列表首次加载、筛选、保存或启停后刷新。',
 [('pageNum','Integer','建议传','页码，沿用现有分页组件'),('pageSize','Integer','建议传','每页条数'),('lotteryTitle / state','String / Integer','可选','沿用列表筛选')],None,dict(list=[admin_detail],total=1),
 ['此接口直接返回 PageResult，即 {list,total}，没有 CommonResult 外层；不要统一假定所有响应都有 data。','列表行 id 是配置 ID，新增 activityId 才是活动主表 ID；启停优先取 activityId。','新增 DTO 字段包含 activityStore/storeIds/storeInfoDTOS，供范围展示。全部门店不能根据 storeIds=[] 误判无门店。'],['LotterySettingsRespVO'])
api('admin-detail','管理后台','GET',base+'/getByDetail','编辑详情（新增标签回显字段）','变更','点击编辑、进入活动详情时；保存后需要最新范围可再次读取。',
 [('id','Long','业务必填','推荐活动主表 ID，兼容配置 ID')],None,common(admin_detail),
 ['data.id 是配置 ID，data.activityId 是活动主表 ID；编辑表单分别保存。','appScope=1 时以 tagIds/tagInfoDTOS 回显选择；storeIds 只是同步后的适用门店。','tagInfoDTOS 中 id/name 用于展示；remark/tagGroupId 目前通常为空，不能依赖它们做标签分组。'],['LotterySettingsDetailDataRespVO'])
api('admin-create','管理后台','POST',base+'/saveLottery','新增活动（增加 appScope/tagIds）','变更','后台新增表单提交；需要 promotion:lottery:add 权限。',
 [('appScope','Integer','建议明确传','0 门店，1 标签；新增不传默认 0'),('tagIds','Long[]','标签模式必填','非空正整数标签值 ID'),('activityStore / storeIds','Integer / Long[]','门店模式按选择','1 全部；0 指定门店'),('其他原表单字段','Object','沿用校验','见完整 LotterySettingsAddReqVO 模型')],scope,common(1),
 ['请求示例仅是本轮新增的范围片段，需合并原有完整新增表单；不是可独立提交的完整请求。','新增保存后后端固定为停用，不会因提交 isEnabled=1 自动启用；需要单独 updateState。','返回 data=1 是操作结果，不是新活动 ID；保存后从列表/详情读取活动 ID。','tagIds 合法但当前没有命中门店，可以保存，不能让空门店集合退化为全部门店。'],['LotterySettingsAddReqVO'])
api('admin-update','管理后台','POST',base+'/update','编辑活动（新增范围选择与状态同步）','变更','后台编辑提交；需要 promotion:lottery:update 权限。',
 [('id','Long','业务必填','详情 data.id：配置 ID'),('activityId','Long','业务必填','详情 data.activityId：活动主表 ID'),('appScope','Integer','建议明确传','0/1；V2 未传时保留原范围'),('tagIds','Long[]','appScope=1 时必填','不能只传 tagInfoDTOS'),('state / isEnabled','Integer','随完整表单同步','0/1，保持一致')],dict(id=admin_detail['id'],activityId=draw['lotteryId'],**scope,state=0,isEnabled=0),common(1),
 ['请求示例是编辑变更片段，需合并原完整编辑表单。此接口不是通用 PATCH，不能只提交这几个字段覆盖原配置。','切换到门店模式时按 activityStore/storeIds 保存，旧标签关系由后端清除；切换到标签模式必须传真实 tagIds。','V2 不传 tagIds/storeIds 时服务有保留逻辑，空数组则是显式空集合。前端建议每次完整提交当前范围，避免混用 null 与 []。','修改已有奖品保留 prizes[].id/code；库存总量不得低于已占用量。有处理中抽奖时奖池模式切换/删除奖品可能被拒绝。','旧活动停用后第一次重新启用才初始化新流程；编辑带 isEnabled=1 且满足重启条件，也会触发该初始化。'],['LotterySettingsUpdateReqVO'])
api('admin-state','管理后台','POST',base+'/updateState','启用/停用（沿用入口，统一生命周期）','变更','后台启停开关点击；需要 promotion:lotteryState:update 权限。',
 [('id','Long','必填','活动主表 ID；取列表 activityId 或详情 activityId'),('isEnabled','Integer','必填','0 停用，1 启用')],dict(id=draw['lotteryId'],isEnabled=1),common(1),
 ['这里字段名是 id，值却是活动主表 ID。不要直接把列表 row.id 填进来。','旧 /updateLotteryState 使用配置 ID + state，新 /updateState 使用活动主表 ID + isEnabled。','新增保存后调用此接口启用；旧活动首次重启进入 V2，V2 活动重复启用不会再次清零已有次数或库存。','接口成功后刷新列表/详情，不要自行修改 runtimeVersion 或清除抽奖缓存。'])
api('tags','关联既有接口','GET','/admin-api/system/tag/value/page','标签值选择器（已有接口）','关联','后台选择“按标签”时加载选择器；需要 system:label:query 权限。',
 [('pageNo','Integer','必填','标签分页页码，注意不同于抽奖列表 pageNum'),('pageSize','Integer','必填','标签每页条数'),('name','String','可选','标签名称筛选'),('tagGroupId','Long','可选','筛选标签组，不是最终提交 tagIds 的值')],None,common(dict(list=[dict(id=scope['tagIds'][0],name='付款下单',tagGroupId=None)],total=1)),
 ['取标签值列表条目的 id 组成 tagIds。也可沿用 /admin-api/system/tag/getTagList（无参数，CommonResult<List<TagValueRespVO>>）。','这是既有 system 接口，不是本轮新增抽奖接口。'])
api('manual-reissue','管理后台','POST','/admin-api/promotion/lottery-log/reissue','人工补发原奖品（不扣次、不扣消耗积分）','新增','后台人员核对原抽奖后调用；登录并持有 promotion:lottery:reissue 权限。普通小程序不能调用。',
 [('lotteryId','Long','必填','原活动主表 ID，兼容配置 ID，来自原请求/详情'),('memberId','Long','必填','原抽奖会员 ID'),('storeId','Long','必填','原抽奖门店 ID，后端核对'),('requestId','String','必填','原抽奖 requestId，不能生成新值'),('reason','String','必填','人工补发原因，最多 200 字')],
 dict(draw,reason='人工核对发放异常，补发原奖品'),common(dict(queued=True,message='已安排人工补发，不扣抽奖次数或消耗积分',result=accepted)),
 ['仅支持已有 V2 流水，按当前项目和原会员/门店核对；没有原流水不创建新抽奖。','不再次扣抽奖次数、活动次数、消耗积分；原失败已退还的保留，尚未完成的原退款先幂等补齐。奖励积分仍按奖品正常发放。','queued=true 只表示后台已安排任务；随后用原 requestId 调用小程序 result 查询进度/结果，该查询仍不触发补发。','已成功、正在执行、补发已排队返回 queued=false 和说明，不重复安排；已退奖、原库存不足等返回业务码 1006005030。','沿用原库存批次和奖品快照；库存已经释放时需重新预占，不能超发，不能指定新奖品、新会员或金额。','红包先查原单：未知/处理中/成功沿用原商户单号；只有明确 FAIL/CANCELLED 才生成补发单号。人工补发再次失败保留原奖品，不自动转其他兜底奖。','原流程如果已切兜底并发放成功，不会再补原抽选奖品；旧流程没有 V2 流水也不能由此接口补发。','发券沿用原快照和有效期，不因补发自动延长。角色权限配置见 sql/lottery_release_all.sql（执行类型 SYSTEM），该脚本未自动执行。'],
 ['LotteryReissueReqVO','LotteryReissueRespVO','LotteryUserLogVO'])

css='''
:root{--bg:#f7f8fa;--ink:#263442;--muted:#647184;--line:#d9e1e8;--get:#1976c9;--post:#258551;--purple:#6858ba}*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:var(--bg);color:var(--ink);font:14px/1.65 "Microsoft YaHei",Arial,sans-serif}header{background:#183b36;color:#fff;padding:23px 32px}header h1{font-size:25px;margin:0 0 5px}header p{margin:0;color:#d1e8e1}.layout{display:grid;grid-template-columns:265px minmax(0,1fr)}nav{position:sticky;top:0;height:100vh;overflow:auto;padding:18px 16px;background:#fff;border-right:1px solid var(--line)}nav a{display:block;color:#395164;text-decoration:none;padding:6px 8px;border-radius:5px}nav a:hover{background:#edf6f2}nav strong{display:block;margin:17px 8px 5px;color:var(--muted);font-size:12px}main{padding:24px 32px 65px;max-width:1500px;width:100%;min-width:0}h2{font-size:21px;margin:29px 0 10px}h3{font-size:16px;margin:20px 0 8px}p{margin:8px 0}.hint{border-left:4px solid #258551;background:#edf7f1;padding:13px 17px}.note{border-left:4px solid #d2a536;background:#fff8e9;padding:13px 17px}.toolbar{display:flex;gap:10px;flex-wrap:wrap;padding:12px 0}input,button,select{font:inherit;border:1px solid #cdd8df;border-radius:5px;padding:7px 11px}input{flex:1;min-width:220px}button{cursor:pointer;background:white;color:var(--ink)}button:hover{background:#edf6f2}.endpoint{border:1px solid var(--line);border-left:4px solid var(--post);border-radius:6px;background:white;margin:12px 0}.endpoint.get{border-left-color:var(--get)}summary{cursor:pointer;padding:13px 15px;list-style:none;display:flex;align-items:center;gap:12px;flex-wrap:wrap}summary::-webkit-details-marker{display:none}summary:after{content:"⌄";margin-left:auto}details[open]>summary{border-bottom:1px solid var(--line)}.method{display:inline-block;min-width:64px;text-align:center;font:bold 12px/28px Arial;border-radius:4px;background:var(--post);color:white}.get .method{background:var(--get)}.path{font-weight:bold;overflow-wrap:anywhere}.desc{color:var(--muted)}.badge{border-radius:4px;font-size:12px;padding:2px 7px;background:#fff1d5;color:#a05c03}.badge.add{background:#e8ddfc;color:var(--purple)}.badge.ref{background:#eef1f4;color:#586673}.body{padding:12px 18px 20px}code{font-family:Consolas,monospace;font-size:13px}.table-scroll{overflow:auto;margin:10px 0 18px}table{width:100%;border-collapse:collapse;min-width:670px;background:white}th,td{text-align:left;vertical-align:top;border:1px solid var(--line);padding:9px 11px}th{background:#eef3f6;font-weight:600}td:first-child{white-space:nowrap}td code{color:#18575b}pre{margin:0 0 16px;overflow:auto;max-height:540px;background:#17252e;color:#dfebe7;padding:16px;border-radius:0 0 5px 5px;font-size:13px;line-height:1.6}.code-label{background:#eaf0f3;padding:5px 12px;border-radius:5px 5px 0 0;display:flex;align-items:center;justify-content:space-between}.copy{font-size:12px;padding:3px 9px}.new{color:#6858ba;font-size:11px}.chips{display:flex;gap:8px;flex-wrap:wrap}.chip{padding:8px 15px;background:white;border:1px solid var(--line);border-radius:5px}.flow{display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin:15px 0}.step{background:white;border:1px solid var(--line);padding:13px;border-top:3px solid #258551}.step b{display:block;margin-bottom:6px}.small{font-size:12px;color:var(--muted)}.schema{margin:12px 0;background:white;border:1px solid var(--line);border-radius:5px}.schema .body{padding-top:0}[hidden]{display:none!important}footer{margin-top:35px;padding-top:15px;border-top:1px solid var(--line);color:var(--muted);font-size:12px}a{color:#177567}@media(max-width:1000px){.layout{grid-template-columns:220px minmax(0,1fr)}main{padding:20px}.flow{grid-template-columns:repeat(2,1fr)}}@media(max-width:720px){header{padding:18px}header h1{font-size:21px}.layout{display:block}nav{position:static;height:auto;border-bottom:1px solid var(--line)}main{padding:16px}summary{gap:8px}.desc{width:100%}.flow{grid-template-columns:1fr}}@media print{nav,.toolbar,.copy{display:none}.layout{display:block}main{padding:0;max-width:none}header{color:#17252e;background:white}header p{color:#647184}details{break-inside:avoid}pre{max-height:none;white-space:pre-wrap}table{min-width:0;font-size:10px}.table-scroll{overflow:visible}}
'''

sections=[]
sections.append('''<section id="overview"><h2>本轮变更范围</h2><div class="hint"><b>前端优先接入：requestId 防重、标签适用范围。</b> 新增接口为小程序 POST /result、GET /recent-winners，以及后台 POST /lottery-log/reissue 人工补发。本轮响应保留 requestId/grantStatus，不输出内部抽奖处理状态。</div><p><b>lottery 直接返回奖品。</b> code=0 且有奖品数据时完成本轮中奖展示，后台异步发放；用户下一次真实抽奖生成新 requestId，不必等待上一轮发奖。</p><p><b>result 只查询，reissue 才安排人工补发。</b> 后台补发有独立权限，不再次扣次数或消耗积分。不要将查询接口当作补发操作。</p><p>适用于 PC 活动配置与小程序抽奖页。可离线打开，支持搜索、折叠和复制 JSON。示例 ID/图片为示意值。</p><p class="small">代码基线 HEAD 4fbb6c2d7，包含响应字段移除和人工补发的本地修改；核对日期 2026-10-08。正常抽奖已有的红包失败转兜底逻辑保留；人工补发独立处理失败，不再次换奖。</p></section>''')
sections.append('''<section id="common"><h2>公共约定与字段来源</h2><div class="note"><b>全部 Long ID 建议保留字符串。</b> 示例中的 19 位 ID 不能经过 JavaScript <code>Number()</code>。响应数字/字符串兼容沿用统一客户端，保证 ID 不损失精度。请求 <code>Content-Type: application/json</code>；登录凭证沿用 <code>Authorization</code>，项目上下文沿用 <code>business-id</code>，不要在 JSON 内额外拼造 businessId。</div>'''+code(common({}))+table(['概念','从哪里取','在哪个接口使用'],[
 ['活动主表 ID','后台列表 <code>activityId</code> / 详情 <code>data.activityId</code>；小程序活动入口/路由上下文','后台 <code>updateState.id</code>、编辑 <code>activityId</code>；小程序推荐作为 <code>lotteryId</code>'],
 ['抽奖配置 ID','后台列表 <code>id</code> / 详情 <code>data.id</code>；小程序 <code>cacheData.id</code>','后台编辑 <code>id</code>，旧启停 <code>updateLotteryState.id</code>'],
 ['标签值 ID','标签选择器条目 <code>id</code>','保存/编辑 <code>tagIds[]</code>；不是标签组 ID'],
 ['抽奖请求标识','前端为一次明确的点击生成并持久保存','<code>lottery.requestId</code>、<code>result.requestId</code> 以及同次重试'],
 ['商户转账单号','抽奖/查询结果 <code>outBillNo</code>','现有红包确认展示；不代替 requestId'],
 ])+'<p>常规成功响应为 <code>{code:0,data:...,msg:""}</code>；业务失败看 <code>code/msg</code>。抽奖活动分页列表为直接 <code>{list,total}</code>，见对应接口。</p></section>')
sections.append('''<section id="flow"><h2>前端调用顺序</h2><div class="flow"><div class="step"><b>1 · 进入抽奖页</b>加载 getLotteryDetailApp、lotteryNum 和 recent-winners。</div><div class="step"><b>2 · 点击抽奖</b>生成 requestId，保存本次上下文，可沿用 verifyLottery，再调用 lottery。</div><div class="step"><b>3 · 展示中奖结果</b>lottery 成功返回奖品后完成本轮动画和结果展示；后台异步发奖。</div><div class="step"><b>4 · 再抽或按需查询</b>再抽生成新 ID；网络异常恢复或红包领取时，用原 ID 调用 result。</div></div><p>后台：列表 → getByDetail → 标签选择器 → 完整表单保存/编辑 → 单独 updateState → 刷新列表。</p></section>''')
sections.append('<section id="click-retry"><h2>再次点击与网络重试</h2>'+table(['场景','调用什么','requestId','是否新增一次抽奖'],[
 ['第一次点击','POST lottery','生成 A','是，受理后占用机会'],
 ['lottery 已成功返回奖品，但后台仍在发放','直接展示中奖；需要领取信息时可查询 result','查询仍使用 A','查询不增加次数'],
 ['lottery 请求超时，不知道是否被受理','先查询 result，必要时重发原 lottery','都用 A，活动/会员/门店不变','已有 A 不重复受理'],
 ['原 HTTP 响应尚未确定时连续点击','前端拦截重复提交，恢复 A','不因重复点击生成新 ID','避免误创建多轮'],
 ['上一轮已拿到中奖结果，用户点“再抽一次”','POST lottery','生成新的 B','是，不等待 A 发奖结束'],
 ['原 A 已处理后，又重发 A','POST lottery / result','仍用 A','返回原请求，不再次抽奖'],
 ])+'<p>只有原请求是否成功还不明确时，才保留待恢复请求、防止误触新抽奖。拿到奖品后，不需要以发奖进度阻塞下一轮。请求限流、次数和积分条件仍由后台检查。</p>'+code('第一次：lottery(A) → 返回奖品，后台异步发放\n再抽一次：lottery(B) → 新一轮\n网络异常恢复：result(A) → 查询 A，不补发\n同次网络重试：lottery(A) → 不重复抽奖','一轮抽奖与多次 HTTP 请求')+'</section>')
sections.append('<section id="rate-limits"><h2>当前限流规则（V2）</h2><p>以下为 LotteryProperties 的代码默认值，也与 Nacos 示例一致；实际环境可能被 promotion.lottery 配置覆盖，本文件未读取线上 Nacos。这里统计请求速率和正在执行的请求，并非每天可抽次数；每日/场次/活动总次数是另一层业务规则。</p>'+table(['限制','默认值','作用范围','配置项'],[
 ['新抽奖请求','100 次 / 1 秒窗口','所有实例共用的 draw 入口 Redis 计数','<code>draw-rate</code>'],
 ['单活动新抽奖请求','60 次 / 1 秒窗口','同项目 + 同活动，所有实例共用','<code>activity-draw-rate</code>'],
 ['单会员新抽奖请求','1 次 / 1 秒窗口','同项目 + 同活动 + 同会员；不是会员所有活动合计','<code>member-draw-rate</code>'],
 ['结果查询请求','300 次 / 1 秒窗口','所有实例共用的 result 入口；lottery 内部查重也使用此入口','<code>result-rate</code>'],
 ['普通查询请求','1000 次 / 1 秒窗口','详情、次数、活动入口、最近中奖等共用 query 计数','<code>query-rate</code>'],
 ['任务请求','200 次 / 1 秒窗口','任务相关入口共用 task 计数','<code>task-rate</code>'],
 ['新抽奖瞬时并发','每实例最多 8（默认配置）','实际上限 min(draw-concurrency, 可用预算)','<code>draw-concurrency</code>'],
 ['结果查询 / 微信回调瞬时并发','每实例合计最多 2','result 和 callback 共用预留名额；回调不计 Redis 速率','当前代码固定预留'],
 ])+'<p>Redis 计数从该键第一次请求开始，设置 1000ms 过期；不是按自然时钟整秒归零，也不是严格滑动窗口。请求超过窗口上限会拒绝；被拒绝的请求也可能已经增加前面步骤的计数，不应连续立即重试。</p><p>默认 connection-budget=16、grant-concurrency=4，再保留 4 个名额，普通请求共享可用预算为 8；详情/任务与新抽奖的瞬时并发还会受该共享预算影响。正在异步发奖的整段等待时间不占着新抽奖请求的执行名额。</p><p><b>防重与限流是两件事：</b>相同 A 已受理时，重发 lottery 优先查询并返回 A，不再作为新 draw 扣次数，也不进入新 draw 速率检查；但查询仍受 result 速率和并发限制。新的 B 会重新经过全局、活动、会员三层新 draw 限流。上一轮已经完成，也不意味着这些限制自动解除。</p><p>限流/并发拒绝返回业务 code=1006000006（目前参与人数过多）。在受理前被拒绝不会扣抽奖次数或预占库存。建议退避重试；A 的结果未知时仍只使用 A，不换 ID。持续运行的旧活动不自动套用 V2 新 draw 这组限流。</p></section>')

newfields=[['新增请求','<code>requestId</code>','<code>lottery/result</code>','前端生成；同次请求复用'],['新增请求','<code>appScope/tagIds</code>','<code>saveLottery/update</code>','表单单选 + 标签选择器'],['新增回显','<code>appScope/tagIds/tagInfoDTOS</code>','<code>getByDetail</code>','已保存范围和标签名'],['新增列表字段','<code>activityId/activityStore/storeIds/storeInfoDTOS/appScope/tagIds/tagInfoDTOS</code>','<code>getLotterySettingsListPage.list[]</code>','活动 ID 与当前适用范围'],['新增响应','<code>requestId/grantStatus</code>','<code>lottery/result.data</code>','后端受理与发奖结果'],['新增详情字段','<code>configVersion/stockEpoch/runtimeVersion/appScope/tagIds</code>','<code>getLotteryDetailApp.data.cacheData</code>','后端维护，只读'],['新增记录字段','<code>isGuarantees</code>','<code>getLotteryLogByMemberId.data[]</code>','中奖时快照，历史可为空'],['新增接口响应模型','<code>memberMobile/prizeName</code>','<code>recent-winners.data[]</code>','脱敏显示信息']]
sections.append('<section id="changes"><h2>新增字段总表</h2>'+table(['变更','字段','出现在何处','来源'],newfields)+'</section>')
for group in dict.fromkeys(x['group'] for x in apis):
    sections.append('<h2 id="group-'+str(len(sections))+'">'+e(group)+'</h2>')
    for a in [a for a in apis if a['group']==group]:
        badge='add' if a['status']=='新增' else 'ref' if a['status']=='关联' else ''
        content='<p><b>调用时机：</b>'+e(a['when'])+'</p><h3>请求参数</h3>'+table(['字段','类型','必填','字段来源 / 含义'],[[f'<code>{e(n)}</code>',e(t),e(r),e(d)] for n,t,r,d in a['params']])
        if a['request'] is not None: content+=code(a['request'],'请求示例' + (' · 范围变更片段，合并原完整表单' if a['key'] in ['admin-create','admin-update'] else ''))
        if a['response'] is not None: content+='<h3>成功响应示例'+('（字段节选）' if a['key'] in ['app-detail','admin-detail','admin-list'] else '')+'</h3>'+code(a['response'])
        else: content+='<p>响应结构/已有字段沿用现有接口；本轮新增字段及行为见下面说明。</p>'
        content+='<h3>前端处理</h3><ul>'+''.join('<li>'+e(n)+'</li>' for n in a['notes'])+'</ul>'
        for m in a['models']: content+=f'<p>完整模型：<a href="#schema-{m}">{m}</a></p>'
        sections.append(f'<details class="endpoint {a["method"].lower()}" id="{a["key"]}" data-kind="{a["status"]}"'+(' open' if a['status']=='新增' else '')+f'><summary><span class="method">{a["method"]}</span><code class="path">{a["path"]}</code><span class="badge {badge}">{a["status"]}</span><span class="desc">{e(a["title"])}</span></summary><div class="body">{content}</div></details>')

statusrows=[['PENDING','后台尚待发放；不阻塞中奖展示或下一次抽奖'],['PROCESSING / ACCEPTED / TRANSFERING / CANCELING','发放/红包处理中；按需查询，不能当作到账成功'],['WAIT_USER_CONFIRM','真实 packageInfo 存在时，提供现有微信确认收款入口'],['SUCCESS','已发放成功'],['FAILED / RETURNED','发放失败或已退回；用于领奖页面与人工排查'],['空/缺失','旧活动按现有领奖流程处理']]
sections.append('<section id="accepted-examples"><h2>中奖结果与发放进度 · 响应示例</h2><p>接口直接返回奖品，同时保留可选的 grantStatus 供领取/排查使用；不要求抽奖页等待发放完成。PENDING 不表示没有抽出奖品。</p>'+code(common(accepted),'示例 1 · lottery 返回中奖奖品，发放待处理')+code(common(dict(accepted,requestId='cash_demo_20261008_001',prizeName='10元红包',prizeType=5,outBillNo='L2104868000000000001',grantStatus='WAIT_USER_CONFIRM',packageInfo='示意值：由微信返回，前端不可自行生成')),'示例 2 · 按需查询红包领取信息')+code(common(dict(accepted,grantStatus='SUCCESS')),'示例 3 · 查询原积分奖品的发放结果')+'<p>示例 1/3 使用同一 requestId，示例 2 是另一轮红包请求。示例中的 ID、奖品、图片及 packageInfo 都是说明值。</p></section>')
sections.append('<section id="statuses"><h2>发放进度与红包领取</h2>'+table(['grantStatus','前端处理'],statusrows)+'<p>发放进度服务于领取入口和异常排查，不是抽奖结果展示条件。普通积分/优惠券/实物中奖无需为了这一字段持续调用 result。</p><p>红包需要后续取得 packageInfo 时可查询 result；确认操作完成后可再查到账状态。result 本身不触发补发。前端不主动调用 /promotion/redPacket/send 发奖，/notify 为微信服务端回调。</p></section>')
sections.append('<section id="scope"><h2>标签/门店切换规则</h2>'+table(['场景','提交字段','前端行为'],[
 ['按标签','<code>appScope=1, activityStore=0, tagIds=[标签值ID...]</code>','tagIds 至少一个有效正数；任意标签命中即可。不能以 storeIds 代替标签。'],
 ['指定门店','<code>appScope=0, activityStore=0, storeIds=[门店ID...], tagIds=[]</code>','门店 ID 从现有门店选择器取得；切换后标签关系由后端清除。'],
 ['全部门店','<code>appScope=0, activityStore=1, storeIds=[], tagIds=[]</code>','空门店关系是全部门店模式表示；不要显示“无可用门店”。'],
 ['标签没有命中门店','<code>appScope=1, tagIds=[有效标签ID]</code>','可以保存；回显适用门店为空，不能展示为全部门店。'],
 ['后台编辑范围','完整表单 + 当前范围字段','成功后重新读列表/详情；门店标签变化由后端同步，前端不请求手动清缓存。'],
 ])+'</section>')

poll='''// 此函数只用于 lottery 超时/断网后的结果恢复；不是每轮强制轮询。
async function restoreLotteryResult(pending) {
  const res = await api.post("/app-api/promotion/lottery-mobile/result", pending);
  if (res.code !== 0) {
    showMessage(res.msg);
    return { keepPending: true };
  }
  if (res.data == null) return { keepPending: true }; // 尚未查到；仍用原 requestId
  clearPending(pending.requestId); // 已确定原请求有中奖数据，不必等待发奖
  renderLotteryPrize(res.data);
  await refreshQuotaAndWinnerLists();
  return { resolved: true, result: res.data };
}
// 正常 lottery 返回奖品后直接展示，下一次明确抽奖生成新的 requestId。
// 红包领取页可另行按需调用 result 查看 grantStatus/packageInfo。
// result 只读，不执行补发；不要把反复查询写成补发操作。'''
sections.append('<section id="examples"><h2>前端结果查询示意</h2>'+code(poll,'JavaScript · 需适配项目客户端')+'</section>')
sections.append('<section id="errors"><h2>错误与兼容处理</h2>'+table(['场景 / 业务码','处理'],[
 ['<code>1006000006</code> 当前参与人数过多','降低频率、退避；存在 pending 的请求继续按相同 requestId 查结果。'],
 ['<code>1006000009</code> 活动总/来源次数达到上限','提示 msg，刷新 lotteryNum；不循环重复发起新抽奖。'],
 ['<code>1006000005</code> 周期次数达到上限','提示 msg，按现有次数限制展示。'],
 ['<code>1006005030</code> 人工补发未受理','检查原流水、项目、会员、门店、退奖状态或原库存；根据 msg 核对，不创建新 requestId 绕过。'],
 ['tagIds 未选 / appScope 非 0、1','提交前校验；服务端具体 code/msg 由统一异常处理产生，不能写死成固定 HTTP 码。'],
 ['不存在 / 不适用门店 / 活动停用或过期','使用服务端 msg，停止页面新抽奖；不要只凭缓存按钮状态绕过。'],
 ['result.data=null 或网络超时','保留 pending，稍后查；需要重发时仅重发原 lottery 及原 requestId。'],
 ['中奖列表暂时不可用','保留已有列表或隐藏展示区；不阻断抽奖/领奖。'],
 ])+'<p>旧活动尚未重启时保持旧流程，不能只凭发布了 V2 代码就假定所有活动都异步；新版活动使用 requestId。既有任务 <code>/task/getTaskList</code>、<code>/task/shareCheck</code>、<code>/task/share</code>、<code>/task/browse</code> 请求仍为 lotteryId/memberId，完成后刷新次数。<code>/browse/complete</code> 和 <code>/browse/count</code> 当前控制器返回空默认对象，不作为新任务入口。<code>/delRedis</code>、<code>/getRedis</code> 已要求后台权限，小程序正常用户不要调用。</p></section>')
sections.append('<section id="schemas"><h2>Schemas · 完整字段模型</h2><p>紫色标记为本轮新增字段。必填列仅说明 Java @NotNull 注解，实际接口还包含业务校验；完整保存表单必须沿用当前页面原校验。请求、响应模型分开，回显对象不等于保存对象。</p></section>')
for name,m in models.items():
    if name == 'LotteryLogDO':
        content=fields(name,True)+'<p>只列本轮新增字段，实体继承的审计/项目字段与已有中奖字段不在本轮文档中展开。</p>'
    else: content=fields(name)
    sections.append(f'<details class="schema" id="schema-{name}"><summary><b>{name}</b><span class="small">{len(m["rows"])} 个直接声明字段</span></summary><div class="body">{content}<p class="small">代码核对：{e(m["source"])}</p></div></details>')
sections.append('''<section id="handoff"><h2>联调检查项</h2><ul><li>标签范围编辑后重新读取，tagIds、标签名与范围类型正确；零命中门店不变成全部门店。</li><li>后台列表 id/activityId 区分正确，updateState 使用活动主表 ID。</li><li>同一个 requestId 重试不出现第二次扣次；断网回来可以恢复结果查询。</li><li>中奖展示与发放进度分开；收到奖品后再抽不等待发放；红包确认、失败排查和旧活动兼容正常。</li><li>最近中奖数组无记录/不足20条正常展示，手机号不再次格式化或还原。</li><li>runtimeVersion/configVersion/stockEpoch 与 tagInfoDTOS/storeInfoDTOS 不混入请求当作新增配置字段。</li></ul><p class="small">本文件依据代码契约生成并静态检查；未调用真实业务接口。环境地址、客户端拆包行为、微信确认逻辑沿用现有前端联调配置。</p></section>''')

navigation='<strong>接入总览</strong>'+''.join(f'<a href="#{i}">{label}</a>' for i,label in [('overview','本轮范围'),('common','ID 与公共约定'),('flow','什么时候调用'),('click-retry','再次点击 / 重试 / result'),('rate-limits','限流与防重的区别'),('changes','新增字段总表')])
for group in dict.fromkeys(x['group'] for x in apis):
    navigation+='<strong>'+e(group)+'</strong>'+''.join(f'<a href="#{a["key"]}">{a["method"]} · {e(a["title"])}</a>' for a in apis if a['group']==group)
navigation+='<strong>模型与处理</strong>'+''.join(f'<a href="#{i}">{label}</a>' for i,label in [('accepted-examples','中奖响应与发放示例'),('statuses','状态与红包'),('scope','标签/门店切换'),('examples','查询代码示意'),('errors','错误与兼容'),('schemas','完整字段模型'),('handoff','联调检查项')])
js='''
const endpoints=[...document.querySelectorAll('.endpoint')];
function filter(){const q=document.querySelector('#search').value.trim().toLowerCase(),kind=document.querySelector('#kind').value;let shown=0;for(const item of endpoints){item.hidden=!(item.textContent.toLowerCase().includes(q)&&(!kind||item.dataset.kind===kind));if(!item.hidden)shown++;}document.querySelector('#count').textContent=`${shown} / ${endpoints.length} 个接口`;}
document.querySelector('#search').addEventListener('input',filter);document.querySelector('#kind').addEventListener('change',filter);
document.querySelector('#expand').onclick=()=>document.querySelectorAll('details').forEach(x=>{if(!x.hidden)x.open=true});document.querySelector('#collapse').onclick=()=>document.querySelectorAll('details').forEach(x=>x.open=false);
document.querySelectorAll('.copy').forEach(b=>b.onclick=async()=>{const text=b.parentElement.nextElementSibling.textContent;try{await navigator.clipboard.writeText(text);b.textContent='已复制';setTimeout(()=>b.textContent='复制',1300);}catch{const range=document.createRange();range.selectNodeContents(b.parentElement.nextElementSibling);const selection=window.getSelection();selection.removeAllRanges();selection.addRange(range);b.textContent='已选中，可 Ctrl+C';}});
function reveal(){const id=location.hash.slice(1),el=document.getElementById(id);if(el){const d=el.closest('details');if(d){d.hidden=false;d.open=true;}el.scrollIntoView();}}window.addEventListener('hashchange',reveal);filter();if(location.hash)reveal();
'''
doc='<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>抽奖 V2 · 前端接口文档</title><style>'+css+'</style></head><body><header><h1>抽奖 V2 · 前端接口文档</h1><p>Swagger 风格 · 新增接口 / 新增字段 / 调用时机 / 字段来源 · 2026-10-08</p></header><div class="layout"><nav>'+navigation+'</nav><main><div class="chips"><span class="chip">3 个新增接口</span><span class="chip">标签范围配置</span><span class="chip">人工补发不扣次/积分</span><span class="chip">离线可分享</span></div><div class="toolbar"><input id="search" type="search" aria-label="搜索接口或字段" placeholder="搜索路径、接口、字段，例如 requestId / tagIds"><select id="kind" aria-label="接口变更类型"><option value="">全部接口</option><option>新增</option><option>变更</option><option>关联</option></select><button id="expand">全部展开</button><button id="collapse">全部折叠</button><span id="count" class="small"></span></div>'+''.join(sections)+'<footer>代码依据：AppLotteryController、LotteryController、TagController、LotteryV2Service、LotteryLedger、LotteryReissueService、LotteryScopeService、LotteryWinnerFeed 及响应 VO。文档描述前端接口契约，并标注旧接口调用边界。</footer></main></div><script>'+js+'</script></body></html>'
OUT.write_text(doc,encoding='utf-8')
(ROOT/'docs/lottery-frontend-contract.json').write_text(json.dumps(dict(baseline='779723b5c',head='4fbb6c2d7',date='2026-10-08',apis=apis,models=models),ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(dict(html=str(OUT),bytes=OUT.stat().st_size,endpoints=len(apis),models=len(models),new_endpoints=sum(a['status']=='新增' for a in apis)),ensure_ascii=False))
