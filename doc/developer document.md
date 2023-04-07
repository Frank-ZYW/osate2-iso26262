# OSATE2 ISO-26262 插件 开发文档

本文档用于构建插件开发环境、介绍插件代码结构，适用于有二次开发或插件维护需求者。

## 开发环境

1. 依照 OSATE2 官网提供的[开发环境安装指南](https://osate.org/setup-development.html)完成 Eclipse 安装。
2. 在 `code` 文件夹内获取项目源代码 `osate2-iso26262.zip` 并解压，导入项目即可。


## 项目结构

本插件主要由8个子插件组成

```shell
osate2-iso26262
├── doc         -- 文档图片、杂项
├── README.md   -- 文档
├── org.osate.iso26262.contrib
├── org.osate.iso26262.example
├── org.osate.iso26262.feature
├── org.osate.iso26262.fmea
├── org.osate.iso26262.fmeda
├── org.osate.iso26262.hazard
├── org.osate.iso26262.lib
└── org.osate.iso26262.ui
```

#### `org.osate.iso26262.contrib`

用于将自定义属性集 `ISO26262.aadl` 在插件安装时自动添加至 OSATE2 工具库，便于项目引用。

#### `org.osate.iso26262.example`

用于将本插件的示例项目添加至 OSATE2 示例项目库，便于示例项目导入。

#### `org.osate.iso26262.feature`

用于插件的打包与安装。

#### `org.osate.iso26262.lib`

公用的第三方库子插件。

#### `org.osate.iso26262.ui`

提供本插件的图形化界面。

```shell
org.osate.iso26262.ui
├── plugin.xml   -- 通过xml文件设置各分析功能按钮的布局和位置
└── ...
```

#### `org.osate.iso26262.hazard`

实现 HAZARD 分析的子插件。

```shell
org.osate.iso26262.hazard
├───build.properties	-- 子插件打包设置
│   plugin.xml        	-- 子插件图形界面与代码调用连接设置
│   pom.xml 		  	-- 项目配置文件
├───icons
│       hazardicon.png	-- HAZARD分析功能按钮图标
├───META-INF
│       MANIFEST.MF    	-- 子插件版本管理、依赖管理
└───src
    └───org
        └───osate
            └───iso26262
                └───hazard
                    │   Activator.java			-- 插件的激活类
                    │   FileExport.java			-- EXCEL报告生成类
                    │   HARAReport.java			-- 分析过程的主体
                    └───handler
                            HARAhandler.java	-- 入口函数
```

#### `org.osate.iso26262.fmea`

实现 FMEA 分析的子插件。

```shell
org.osate.iso26262.fmea
├───build.properties	-- 子插件打包设置
│   plugin.xml        	-- 子插件图形界面与代码调用连接设置
│   pom.xml				-- 项目配置文件
├───icons
│       FMEAicon.png	-- FMEA分析功能按钮图标
├───META-INF
│       MANIFEST.MF		-- 子插件版本管理、依赖管理
└───src
    └───org
        └───osate
            └───iso26262
                └───fmea
                    │   Activator.java  		-- 插件的激活类
                    │   AP.java					-- Action Priority 行动优先级定义类
                    │   ASIL.java				-- ASIL 安全完整性等级定义类
                    │   FailureElement.java		-- 组件的故障元素定义类
                    │   FmeaBuilder.java		-- FMEA分析功能类
                    │   FmeaHead.java			-- FMEA表头定义类
                    │   FMEDAPI.java			-- 供FMEDA插件使用的API
                    │   Function.java			-- 组件的功能定义类
                    │   Optimization.java		-- 故障元素优化定义类
                    │   SafetyCategory.java		-- 安全需求种类定义类
                    │   Structure.java			-- 组件结构定义类
                    ├───export
                    │       Error_item.java		-- 输出的单个故障分析条目的定义类
                    │       FileExport.java 	-- Excel格式分析报告生成功能类
                    │       Func_item.java		-- 输出的单个功能分析条目的定义类
                    │       Pair.java			-- Pair模板定义
                    │       Struc_item.java		-- 输出的结构分析条目的定义类
                    ├───fixfta
                    │       CreateFTAModel.java	-- 故障模型生成的功能类
                    │       FaultTreeUtils.java	-- 故障树分析工具类
                    │       FTAGenerator.java	-- 故障树生成器
                    │       PropagationGraphBackwardTraversal.java -- 故障传播图生成类
                    └───handler
                            FmeaDialog.java		-- 插件交互界面定义类
                            FMEAhandler.java	-- FMEA分析入口
```

#### `org.osate.iso26262.fmeda`

实现 FMEDA 分析的子插件。

```shell
org.osate.iso26262.fmeda
├── build.properties  -- 子插件打包设置
├── icons
│   └── fmeda.png     -- FMEDA分析功能按钮图标
├── META-INF
│   └── MANIFEST.MF   -- 子插件版本管理、依赖管理
├── plugin.xml        -- 子插件图形界面与代码调用连接设置
├── pom.xml			  -- 项目配置文件
└── src
    └── org
        └── osate
            └── iso26262
                └── fmeda
                    ├── Activator.java		  -- 插件的激活类
                    ├── FmedaFaultMode.java   -- 失效模式定义类 
                    ├── FmedaProperty.java    -- 公共属性定义类 
                    ├── FmedaTable.java       -- 分析报告数据定义类 
                    ├── handler
                    │   ├── ErrorDialog.java   -- 分析错误弹窗定义类 
                    │   ├── FmedaDialog.java   -- FMEDA分析图形交互界面定义类
                    │   └── FmedaHandler.java  -- 分析过程的主体、入口函数
                    ├── report
                    │   ├── CsvReportGenerator.java     -- CSV格式分析报告生成功能类
                    │   ├── ExcelReportGenerator.java   -- Excel格式分析报告生成功能类
                    │   └── FmedaReportGenerator.java   -- 分析报告生成基类
                    └── util
                        ├── CalculationUtil.java     -- FMEDA分析数据计算工具类
                        ├── PropertyParseUtil.java   -- 自定义属性集解析工具类
                        └── ReportUtil.java          -- 分析报告生成字符串处理工具类
```

*注：源码各函数功能详见代码注释*

