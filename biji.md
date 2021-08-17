## 笔记
### 1. 初步分析

#### 1.1 formProperties
在bpmn的审批节点上, 配置了表单属性字段, Form properties, 这个字段的格式是这样的:
```
 // FormProperty_3qipis2--__!!radio--__!!审批意见--__!!i--__!!同意--__--不同意
 //FormProperty_0lffpcm--__!!textarea--__!!批注--__!!f__!!null
 //radio--__!!p--__!!同意--__--不同意
 //textarea--__!!f--__!!null
 //complaint_consumer_service_check--__!!select--__!!处理类型--__!!i--__!!ACT_FLOW_complaint_consumer_handle_type
```
采用`--__!!`分割开, 第一个是id, 第二个是字段类型, 第三个是字段名称, 第四个表示是否为参数, 如果不是`f`,
则表示这个作为变量来completeTask,第五个是默认值. 其中`--__--`表示, 如果是单选框类型, 则这个符号分隔开两个选择,
通过value的下标来选择.

目前支持 text, textarea, select, radio.   
其中radio, 是通过第五个默认值来实现, `--__--`分割, 下标序号作为实际值.  
select是通过第五个默认值, 这里的默认值是字典中的type名. 即表`sys_dict_type`中的dict_type. 会去取这个字典的枚举作为下拉框


### 2. 整体流程
1. 创建bpmn流程图, 将审批人assignee,或者候选人设置好, 审批节点上的formProperties加上. 后面判断分支采用的变量是formProperties的id
2. 发起流程. 生成第一个task, formProperties也随流程启动而绑定
3. 表单在点击操作流程的时候, 会请求后台接口查询task信息, 其中包括formProperties信息, 解析出字段类型, 名称,
当前默认值, 然后通过vue的双向绑定展示出来
4. 在页面填写值以后点击审批提交, 此时form将新的formProperties也一起提交.
5. 后台解析formProperties的dto, 然后通过variable参数绑定到task. 同时将form信息保存到自定义的表中. ACT_WORK_FLOWDATA
6. task走到某个分支, 通过variable绑定的值以及配置的表达式选择分支
7. 流程走完后, 查看审批信息, 从ACT_WORK_FLOWDATA中查询

### 3. 制作流程图
1. 在自带的流程图设计网页上制作, 不同的流程图设计工具, 不一定兼容, 会丢失属性
2. formProperties的命名规则要遵循上面提到的规则, 拼接成字符串后作为id. (为什么不填入其他属性,比如字段名, 字段默认值, 字段类型, 因为可能其他设计图打开没有这几个字段)
3. formKey必须要填, 且要和这个框框(任务节点)的id一样. 这是因为后面取这个节点的formProperties的时候参数需要任务id，但是无法获取，只能获取表单key“task.getFormKey()”当做任务key

### 4. 查看审批进度
查看审批进度涉及到3个接口.
1. /processDefinition/getDefinitions/{id}, 参数是instanceId, 返回 部署id 和 流程资源名
2. /activitiHistory/gethighLine, 参数是instanceId, 返回highPoint, highLine等信息
3. /processDefinition/getDefinitionXML, 参数是部署id, 和资源名称. 返回流程的完整xml信息

## 本项目未实现
### 1. 知会
知会的功能, 本身是通知和流程处理无关的人, 所以本身可以直接不依赖任何activiti的表, 创建一个业务表, 记录知会人, 以及流程的businessKey, 相当于流程id即可.  
或者是通过createTask功能, 凭空创建一个任务, 然后assignee设置为知会人, 但是由于手动创建task无法关联流程businessKey, 所以需要变通比如将taskName设置为
需要通知的流程key. 然后被知会人登录后, 查询task的方法中可以查到这个知会, 但是要做好区分, 这个task记录没有businessKey, 通过taskName取到对应的流程返回给前端展示

### 2. 会签
会签包括 并行 会签, 串行会签, 流程的逻辑控制也需要判断整个会签结果
#### 2.1 串行并行
可以通过流程设计的时候, 选择`Multi-instance type`实现, parallel是并行, sequential是串行
#### 2.2 设置会签人
会签人是个集合, 设置`Collection(Multi-instance)`为一个集合, 以及`Element variable(Multi-instance)`作为集合中单个元素的变量名,再设置Assignee为这个变量名.
#### 2.3 会签条件
1. 可以用activiti自带的判断, `Completion condition(Multi-instance)`, 只要满足条件, 就会结束该节点. 
2. 如果需要在会签后再做一些其他的判断, 可以在会签的节点加监听器, 然后监听类来判断是否满足条件, 满足后设置一个绑定变量. 在这个节点后增加变量的判断网关.
> 1.    这里需要介绍一下会签环节中设计的几个默认流程变量
> 2.    nrOfInstances（numberOfInstances）：会签中总共的实例数
> 3.    nrOfCompletedInstances：已经完成的实例数量
> 4.    nrOfActiviteInstances：当前活动的实例数量，即还没有完成的实例数量  
> 条件${nrOfInstances == nrOfCompletedInstances}表示所有人员审批完成后会签结束。  
> 条件${ nrOfCompletedInstances == 1}表示一个人完成审批，该会签就结束。

### 3. 驳回
#### 3.1 单个执行人
单个执行人的驳回，用连线方式可以解决, 直接将不同意的流向指回原来的节点即可
#### 3.2 多个执行人 
如果是多个执行人, 则需要无法直接用连线了. 因为