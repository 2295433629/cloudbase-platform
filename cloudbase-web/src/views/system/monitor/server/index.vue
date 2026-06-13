<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" v-loading="loading">
          <template #header><span>💻 CPU</span></template>
          <div v-if="info.cpu">
            <p>处理器: {{ info.cpu.cpuName }}</p>
            <p>核心: {{ info.cpu.physicalCores }}物理 / {{ info.cpu.logicalCores }}逻辑</p>
            <el-progress :percentage="parseFloat(info.cpu.systemUsage)" :color="cpuColor" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>🧠 内存</span></template>
          <div v-if="info.memory">
            <p>总内存: {{ info.memory.total }}</p>
            <p>已用: {{ info.memory.used }} / 可用: {{ info.memory.available }}</p>
            <el-progress :percentage="parseFloat(info.memory.usage)" :color="memColor" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>☕ JVM</span></template>
          <div v-if="info.jvm">
            <p>Java版本: {{ info.jvm.javaVersion }}</p>
            <p>最大内存: {{ info.jvm.maxMemory }}</p>
            <p>已用: {{ info.jvm.usedMemory }} / 总分配: {{ info.jvm.totalMemory }}</p>
            <el-progress :percentage="parseFloat(info.jvm.usage)" :color="jvmColor" />
            <p style="margin-top:8px;color:#999">运行时长: {{ info.jvm.runTime }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top:20px" v-if="info.sys">
      <template #header><span>🖥️ 系统信息</span></template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="操作系统">{{ info.sys.osName }}</el-descriptions-item>
        <el-descriptions-item label="架构">{{ info.sys.osArch }}</el-descriptions-item>
        <el-descriptions-item label="主机名">{{ info.sys.hostName }} / {{ info.sys.hostIp }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="hover" style="margin-top:20px" v-if="info.disks">
      <template #header><span>💾 磁盘信息</span></template>
      <el-table :data="info.disks" border>
        <el-table-column prop="dirName" label="挂载点" />
        <el-table-column prop="typeName" label="类型" />
        <el-table-column prop="total" label="总空间" />
        <el-table-column prop="free" label="可用" />
        <el-table-column prop="used" label="已用" />
        <el-table-column prop="usage" label="使用率">
          <template #default="{ row }"><el-progress :percentage="parseFloat(row.usage)" :color="diskColor(row)" /></template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const info = ref({})
const loading = ref(true)

const fetchData = () => {
  api.post('/sys/monitor/server').then(res => { info.value = res; loading.value = false })
}
onMounted(fetchData)

const cpuColor = (pct) => pct > 80 ? '#F56C6C' : pct > 60 ? '#E6A23C' : '#67C23A'
const memColor = (pct) => pct > 80 ? '#F56C6C' : pct > 60 ? '#E6A23C' : '#67C23A'
const jvmColor = (pct) => pct > 80 ? '#F56C6C' : pct > 60 ? '#E6A23C' : '#67C23A'
const diskColor = (row) => parseFloat(row.usage) > 80 ? '#F56C6C' : parseFloat(row.usage) > 60 ? '#E6A23C' : '#67C23A'
</script>
