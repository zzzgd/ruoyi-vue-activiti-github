<template>
  <div>
    <div>
      <h2>申请人：{{ form.createName }}</h2>
      <el-form label-width="80px">
        <el-form-item label="工单单号">
          <el-input v-model="form.serialNo"/>
        </el-form-item>
        <el-form-item label="工单描述">
          <el-input v-model="form.remark"/>
        </el-form-item>
        <el-form-item label="审批类型">
          <el-input v-model="form.type"/>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-input v-model="form.commitTime"/>
        </el-form-item>
      </el-form>
    </div>
    <div v-for="(historyData, index) in fromData"
         :key="index">
      <h2>{{ historyData.taskNodeName }}</h2>
      <h3>审批人:{{ historyData.createName }}</h3>
      <h3>审批时间:{{ historyData.createdDate }}</h3>
      <el-form v-for="(fistoryFormData, indexH) in historyData.formHistoryDataDTO" :key="indexH" label-width="80px">
        <el-form-item :label=fistoryFormData.title>
          <el-input v-model="fistoryFormData.value" />

        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import {getCustomercomplaint} from '@/api/workflow/customercomplaint'
import {historyFromData} from '@/api/activiti/historyFormdata'

export default {
  name: "customercomplaintHistoryForm",
  props: {
    businessKey: {
      type: String
    }
  },
  data() {
    return {
      // 表单参数
      form: {},
      fromData: [],
    }
  },
  created() {
    this.getCustomercomplaint()
    this.historyFromData()
  },
  methods: {
    getCustomercomplaint() {
      getCustomercomplaint(this.businessKey).then(response => {
        this.form = response.data
      })
    },
    historyFromData() {
      historyFromData(this.businessKey).then(response => {
        this.fromData = response.data
      })
    },
  }

}
</script>

<style scoped>

</style>
