# 📁 GitHub 上传文件详细步骤

## 方法1：网页上传（推荐新手）

### 步骤：
1. 在你的 `privacy-policy` 仓库页面
2. 点击 **"Add file"** → **"Upload files"**
3. 将 `privacy_policy.html` 拖拽到上传区域
4. 在页面底部填写：
   - **Commit message**: `Add privacy policy`
   - **Description**: `Initial privacy policy for Floating Clock app`
5. 点击 **"Commit changes"**

## 方法2：直接创建文件

### 步骤：
1. 在仓库页面点击 **"Add file"** → **"Create new file"**
2. 文件名输入：`index.html`
3. 复制下面的完整HTML代码到编辑器
4. 滚动到底部，填写提交信息
5. 点击 **"Commit changes"**

---

# 🌐 启用 GitHub Pages

## 设置步骤：
1. 在仓库页面点击 **"Settings"** 标签
2. 向下滚动找到 **"Pages"** 部分
3. 在 **"Source"** 下拉菜单选择 **"Deploy from a branch"**
4. **Branch** 选择 **"main"**
5. **Folder** 保持 **"/ (root)"**
6. 点击 **"Save"**

## 获取URL：
- 几分钟后，页面会显示：
  **"Your site is published at: https://用户名.github.io/privacy-policy"**
- 这就是你的隐私政策URL！

---

# ✅ 验证步骤

1. 等待2-5分钟让GitHub部署
2. 访问你的URL确认页面正常显示
3. 复制URL到Google Play Console的隐私政策字段

---

# 🔧 故障排除

## 如果页面不显示：
1. 确认文件名是 `index.html`（不是 `privacy_policy.html`）
2. 等待更长时间（最多10分钟）
3. 检查 Settings → Pages 是否正确配置
4. 尝试访问：`https://用户名.github.io/privacy-policy/index.html`

## 如果需要修改：
1. 直接在GitHub上编辑文件
2. 提交更改
3. 几分钟后更新会生效