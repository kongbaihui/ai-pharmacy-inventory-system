<template>
  <div>
    <div class="user-info-head" @click="editCropper">
      <img :src="options.img" title="点击上传头像" class="img-circle img-lg">
    </div>
    <el-dialog
      :title="title"
      :visible.sync="open"
      width="800px"
      append-to-body
      @opened="modalOpened"
      @close="closeDialog"
    >
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            v-if="visible"
            ref="cropper"
            :img="options.img"
            :info="true"
            :auto-crop="options.autoCrop"
            :auto-crop-width="options.autoCropWidth"
            :auto-crop-height="options.autoCropHeight"
            :fixed-box="options.fixedBox"
            :output-type="options.outputType"
            @realTime="realTime"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="previews.url" :style="previews.img">
          </div>
        </el-col>
      </el-row>
      <br>
      <el-row>
        <el-col :lg="2" :sm="3" :xs="3">
          <el-upload action="#" :http-request="requestUpload" :show-file-list="false" :before-upload="beforeUpload">
            <el-button size="small">选择<i class="el-icon-upload el-icon--right" /></el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :sm="2" :xs="2">
          <el-button icon="el-icon-plus" size="small" @click="changeScale(1)" />
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :sm="2" :xs="2">
          <el-button icon="el-icon-minus" size="small" @click="changeScale(-1)" />
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :sm="2" :xs="2">
          <el-button icon="el-icon-refresh-left" size="small" @click="rotateLeft" />
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :sm="2" :xs="2">
          <el-button icon="el-icon-refresh-right" size="small" @click="rotateRight" />
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :sm="2" :xs="2">
          <el-button type="primary" size="small" @click="uploadImg">提交</el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script>
import store from '@/store'
import { VueCropper } from 'vue-cropper'
import { uploadAvatar } from '@/api/system/user'
import { debounce } from '@/utils'

export default {
  components: { VueCropper },
  data() {
    return {
      open: false,
      visible: false,
      title: '修改头像',
      options: {
        img: store.getters.avatar,
        autoCrop: true,
        autoCropWidth: 200,
        autoCropHeight: 200,
        fixedBox: true,
        outputType: 'png',
        filename: 'avatar'
      },
      previews: {},
      resizeHandler: null
    }
  },
  methods: {
    editCropper() {
      this.open = true
    },
    modalOpened() {
      this.visible = true
      if (!this.resizeHandler) this.resizeHandler = debounce(this.refresh, 100)
      window.addEventListener('resize', this.resizeHandler)
    },
    refresh() {
      if (this.$refs.cropper) this.$refs.cropper.refresh()
    },
    requestUpload() {},
    rotateLeft() {
      this.$refs.cropper.rotateLeft()
    },
    rotateRight() {
      this.$refs.cropper.rotateRight()
    },
    changeScale(number) {
      this.$refs.cropper.changeScale(number || 1)
    },
    beforeUpload(file) {
      if (file.type.indexOf('image/') === -1) {
        this.$modal.msgError('文件格式错误，请上传 JPG 或 PNG 图片。')
        return false
      }
      const reader = new FileReader()
      reader.readAsDataURL(file)
      reader.onload = () => {
        this.options.img = reader.result
        this.options.filename = file.name
      }
      return true
    },
    uploadImg() {
      this.$refs.cropper.getCropBlob(data => {
        const formData = new FormData()
        formData.append('avatarfile', data, this.options.filename)
        uploadAvatar(formData).then(response => {
          this.open = false
          this.options.img = process.env.VUE_APP_BASE_API + response.imgUrl
          store.commit('SET_AVATAR', this.options.img)
          this.$modal.msgSuccess('修改成功')
          this.visible = false
        })
      })
    },
    realTime(data) {
      this.previews = data
    },
    closeDialog() {
      this.options.img = store.getters.avatar
      this.visible = false
      window.removeEventListener('resize', this.resizeHandler)
    }
  }
}
</script>

<style lang="scss" scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
}

.user-info-head:hover::after {
  content: '+';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  cursor: pointer;
  font-size: 24px;
  line-height: 110px;
}
</style>
