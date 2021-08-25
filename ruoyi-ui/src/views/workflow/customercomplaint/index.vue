<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="工单号" prop="title">
        <el-input
          v-model="queryParams.serialNo"
          placeholder="请输入工单号"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item label="状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="请选择状态" clearable size="small">
          <el-option
            v-for="dict in stateOptions"
            :key="dict.dictValue"
            :label="dict.dictLabel"
            :value="dict.dictValue"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审核类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择审核类型" clearable size="small">
          <el-option
              v-for="dict in approveTypeOptions"
              :key="dict.dictValue"
              :label="dict.dictLabel"
              :value="dict.dictValue"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="cyan" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['workflow:customercomplaint:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['workflow:customercomplaint:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customercomplaintList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <!--      <el-table-column label="主键ID" align="center" prop="id" />-->
      <el-table-column label="工单单号" align="center" prop="serialNo"/>
      <el-table-column label="审批类型" align="center" prop="type" :formatter="typeFormat"/>
      <el-table-column label="工单描述" align="center" prop="remark"/>
      <el-table-column label="提交时间" align="center" prop="commitTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.commitTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>

      <!--      <el-table-column label="状态" align="center" prop="state" :formatter="stateFormat">-->
      <el-table-column label="状态" align="center">
        <template slot-scope="scope">
          <div v-if="scope.row.state!=0">
            {{stateFormat(scope.row)}}
          </div>
          <div v-else>
            {{scope.row.taskName}}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="commitTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>

      <!--      <el-table-column label="创建者" align="center" prop="createName" />-->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-if="2==scope.row.state"
                     size="mini"
                     type="text"
                     icon="el-icon-edit"
                     @click="handleUpdate(scope.row)"
                     v-hasPermi="['workflow:customercomplaint:edit']"
          >修改
          </el-button>
                    <el-button v-if="1!=scope.row.state"
                      size="mini"
                      type="text"
                      icon="el-icon-edit"
                      @click="handleDelete(scope.row)"
                      v-hasPermi="['workflow:customercomplaint:remove']"
                    >删除
                    </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="historyFory(scope.row)"
            v-hasPermi="['workflow:customercomplaint:edit']"
          >审批详情
          </el-button>

          <el-button v-if="0==scope.row.state"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="checkTheSchedule(scope.row)"
            v-hasPermi="['workflow:customercomplaint:edit']"
          >查看进度
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog
      :visible.sync="modelVisible"
      title="进度查询"
      width="1680px"
      append-to-body
    >
      <div style="position:relative;height: 100%;">
        <iframe
          id="iframe"
          :src="modelerUrl"
          frameborder="0"
          width="100%"
          height="720px"
          scrolling="auto"
        ></iframe>
      </div>
    </el-dialog>

    <!-- 查看详细信息话框 -->
    <el-dialog :title="title" :visible.sync="open2" width="500px" append-to-body>
      <customercomplaintHistoryForm :businessKey="businessKey" v-if="open2"/>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open2=!open2">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工单号" prop="serialNo">
          <el-input v-model="form.serialNo" placeholder="请输入工单号" clearable :style="{width: '100%'}">
          </el-input>
        </el-form-item>

        <el-form-item label="审核类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择审核类型" >
            <el-option
                v-for="dict in approveTypeOptions"
                :key="dict.dictValue"
                :label="dict.dictLabel"
                :value="dict.dictValue"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="工单描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入工单描述"
                    :autosize="{minRows: 4, maxRows: 4}" :style="{width: '100%'}"></el-input>
        </el-form-item>
        <el-form-item label="提出日期" prop="commitTime">
          <el-date-picker type="datetime" v-model="form.commitTime" format="yyyy-MM-dd HH:mm:ss"
                          value-format="yyyy-MM-dd HH:mm:ss" :style="{width: '100%'}" placeholder="请选择提出日期" clearable>
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {listCustomercomplaint, getCustomercomplaint, delCustomercomplaint, addCustomercomplaint, updateCustomercomplaint, exportCustomercomplaint} from '@/api/workflow/customercomplaint'
  import {getUserProfile} from '@/api/system/user'
  import {getDefinitionsByInstanceId} from '@/api/activiti/definition'


  import customercomplaintHistoryForm from "./customercomplaintHistoryForm";

  export default {
    name: 'customercomplaint',
    components: {customercomplaintHistoryForm},
    data() {
      return {
        modelVisible: false,
        modelerUrl: '',
        userName: '',
        createName:'',
        businessKey: '',
        //用户信息
        user: {},
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 请假表格数据
        customercomplaintList: [],
        // 弹出层标题
        title: '',
        // 是否显示弹出层
        open: false,
        open2: false,
        // 请假类型字典
        approveTypeOptions: [],
        // 状态字典
        stateOptions: [],
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          type: null,
          serialNo: null,
          remark: null,
          commitTime: null,
          instanceId: null,
          state: null,
          createBy: null
        },
        rules: {
          serialNo: [{
            required: true,
            message: '请输入工单号',
            trigger: 'blur'
          }],
          type: [{
            required: true,
            message: '请选择审核类型',
            trigger: 'change'
          }],
          remark: [{
            required: true,
            message: '请输入多行文本工单描述',
            trigger: 'blur'
          }],
          commitTime: [{
            required: true,
            message: '请选择提出日期',
            trigger: 'change'
          }],
        },
        // 表单参数
        form: {},
        // 表单校验
      }
    },
    created() {
      this.getList()
      this.getDicts('activiti_customercomplaint_type').then(response => {
        this.approveTypeOptions = response.data
      })
      this.getDicts('activiti_flow_type').then(response => {
        this.stateOptions = response.data
      })
    },
    methods: {
      /** 查询请假列表 */
      getList() {
        this.loading = true
        listCustomercomplaint(this.queryParams).then(response => {
          this.customercomplaintList = response.rows
          this.total = response.total
          this.loading = false
        })
      },
      // 请假类型字典翻译
      typeFormat(row, column) {
        return this.selectDictLabel(this.approveTypeOptions, row.type)
      },
      // 状态字典翻译
      stateFormat(row, column) {
        return this.selectDictLabel(this.stateOptions, row.state)
      },

      // 取消按钮
      cancel() {
        this.open = false
        this.reset()
      },
      // 表单重置
      reset() {
        this.form = {
          id: null,
          type: null,
          title: null,
          reason: null,
          customercomplaintStartTime: null,
          customercomplaintEndTime: null,
          instanceId: null,
          state: null,
          createBy: null,
          createTime: null,
          updateTime: null
        }
        this.resetForm('form')
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1
        this.getList()
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.resetForm('queryForm')
        this.handleQuery()
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.createName = this.$store.getters.nickName
        if (this.$store.getters.name != "admin") {
          this.reset()
          this.open = true
          this.title = '添加客诉工单'
        } else {
          this.$alert('管理员不能创建流程', '管理员不能创建流程', {
            confirmButtonText: '确定',
          });
        }

      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset()
        getCustomercomplaint(row.id).then(response => {
          this.form = response.data
          this.open = true
          this.title = '修改客诉工单'
        })
      },
      /** 审批详情 */
      historyFory(row) {
        this.businessKey = row.id
        this.open2 = true
        this.title = '审批详情'

      },
      /** 进度查看 */
      checkTheSchedule(row) {
        getDefinitionsByInstanceId(row.instanceId).then(response => {
          let data = response.data
          // this.url = '/bpmnjs/index.html?type=lookBpmn&deploymentFileUUID='+data.deploymentID+'&deploymentName='+ encodeURI(data.resourceName);
          this.modelerUrl = '/bpmnjs/index.html?type=lookBpmn&instanceId=' + row.instanceId + '&deploymentFileUUID=' + data.deploymentID + '&deploymentName=' + encodeURI(data.resourceName);
          this.modelVisible = true
        })


      },
      /** 提交按钮 */
      submitForm() {
        this.$refs['form'].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateCustomercomplaint(this.form).then(response => {
                this.msgSuccess('修改成功')
                this.open = false
                this.getList()
              })
            } else {
              addCustomercomplaint(this.form).then(response => {
                this.msgSuccess('新增成功')
                this.open = false
                this.getList()
              })
            }
          }
        })
      },
      /** 删除按钮操作 */
      handleDelete(row) {
        const ids = row.id || this.ids
        this.$confirm('是否确认删除客诉工单编号为"' + ids + '"的数据项?', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(function () {
          return delCustomercomplaint(ids)
        }).then(() => {
          this.getList()
          this.msgSuccess('删除成功')
        })
      },
      /** 导出按钮操作 */
      handleExport() {
        const queryParams = this.queryParams
        this.$confirm('是否确认导出所有客诉工单数据项?', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(function () {
          return exportCustomercomplaint(queryParams)
        }).then(response => {
          this.download(response.msg)
        })
      },
      chooseMedicine() {
        this.form.title = this.createName + "的" + this.form.type + "申请";
      }
    }

  }
</script>
