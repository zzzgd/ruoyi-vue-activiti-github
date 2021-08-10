## 笔记
### 1. 初步分析

#### 1.1 formProperties
在bpmn的审批节点上, 配置了表单属性字段, Form properties, 这个字段的格式是这样的:
```
 // FormProperty_3qipis2--__!!radio--__!!审批意见--__!!i--__!!同意--__--不同意
 
 //radio--__!!p--__!!同意--__--不同意
 //textarea--__!!f--__!!null
```
采用`--__!!`分割开, 第一个是id, 第二个是字段类型, 第三个是字段名称, 第四个表示是否为参数, 如果不是`f`,
则表示这个作为变量来completeTask,第五个是默认值. 其中`--__--`表示, 如果是单选框类型, 则这个符号分隔开两个选择,
通过value的下标来选择.


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