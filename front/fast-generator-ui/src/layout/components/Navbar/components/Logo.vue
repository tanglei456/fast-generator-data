<template>
	<div class="sidebar-logo">
		<!-- <el-avatar src="./favicon.ico"></el-avatar> -->
		<span class="logo-title">Data Generator</span>
		<a uri=""></a>
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
	box-shadow: rgba(30, 117, 199, 0.02) 0 1px 4px;
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
