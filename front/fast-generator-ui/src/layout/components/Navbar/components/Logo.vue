<template>
	<div class="sidebar-logo">
		<el-avatar src="src/assets/logo.ico"></el-avatar>
		<span class="logo-title">Data Generator</span>
	</div>
</template>

<style lang="scss" scoped>
.sidebar-logo {
	width: var(--theme-aside-width) !important;
	height: var(--theme-header-height);
	line-height: var(--theme-header-height);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: rgba(167, 182, 173, 0.02) 0 1px 4px;
	color: var(--theme-logo-text-color);
	font-size: 18px;
	.el-avatar {
		width: 25px;
		height: 25px;
	}
	.logo-title {
		margin-left: 10px;
	}
}
</style>
