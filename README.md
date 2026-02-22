# lqx-web
npm install 
npm run dev


### 项目介绍
柳琴非遗运营系统说明文档
一、系统概览
本系统用于“柳琴非遗”线上运营与管理，覆盖公共门户、用户中心、传承与师承教学、资源库、社区互动、活动票务、商城交易、积分体系以及后台运营管理等完整业务闭环。
前端采用 Vue 3 + Vite + Element Plus，后端基于 Spring Boot 3 + MyBatis-Plus + JWT 鉴权，数据库为 MySQL。
二、系统架构
• 前端：Vue3、Pinia 状态管理、Vue Router 路由、Element Plus 组件库；统一通过 /api 代理访问后端。
• 后端：Spring Boot REST API，MyBatis-Plus 数据访问，JWT 登录态与角色权限控制，Multipart 文件上传。
• 数据库：业务库 liuqin_opera_db（见根目录 liuqin_opera_db.sql），覆盖用户、传承人资料、资源、活动、票务、商品及订单、社区、积分等表。
三、角色与权限
系统采用 JWT + 自定义 @RequireRole 注解实现鉴权与授权，拦截器解析令牌并将 userId、username、role 注入请求上下文。
• 角色编码：0 普通用户、1 传承人（师父/卖家）、2 审核员（运营）、3 管理员。
• 未声明权限的接口允许匿名访问；声明了 @RequireRole 的接口必须携带有效令牌且角色匹配。
四、功能模块
4.1 公共门户与首页内容
• 首页内容：GET /api/home/content?type=... 提供不同类型的首页位内容（轮播、推荐等）。
• 公共用户页：/api/public/user 提供公开的用户档案、传承人信息、其资源、商品、活动等聚合数据。
4.2 认证与用户管理
• 登录认证：/api/auth 登录生成 JWT，包含 userId/username/role；密码采用 BCrypt 加密。
• 用户中心：/api/users 提供用户信息查询与修改；管理员可分配角色、创建/删除用户。
• 地址管理：/api/user/address 管理用户收货地址，用于订单与发货场景。
4.3 传承人与审核
• 传承人资料：/api/inheritor 维护等级、流派、艺术经历、获奖情况与审核状态。
• 运营审核：/api/admin/inheritor 由审核员/管理员对传承人资料进行审核与状态变更。
• 前端“传承人申请”支持预填、状态提示（待审/通过/驳回），并与师承模块联动。
4.4 师承与教学
• 拜师申请：/api/master/apprentice 提交与审核徒弟申请（传承人角色可分页查看待审申请、批准/驳回）。
• 教学任务：/api/teaching 师父发布任务、查看徒弟列表、查看提交情况、点评作业、学生提交作业与查看自己的作业列表。
• 作业互动：作业支持官方点评与社区评论聚合展示，便于形成教学闭环。
4.5 非遗资源库
• 资源管理：/api/resources 用户与传承人可提交资源（视频、图文等），记录作者与审核状态。
• 资源审核：/api/admin/resources 审核员进行资源内容治理与上下架控制。
• 资源观看：与积分体系联动，记录每日观看奖励，鼓励学习与传播。
4.6 社区与评论
• 帖子：/api/community 提供帖子列表、详情、发布与治理（管理员端 /api/admin/post）。
• 评论：/api/community/comment 用户可对资源、帖子、作业等进行评论；支持删除本人评论与后台审核管理。
4.7 活动与票务
• 活动管理：/api/events 管理演出活动，包含时间地点、票类与库存、状态控制等。
• 票务购买：/api/ticket 购票、订单生成、核销逻辑；与用户信息及地址协同。
4.8 商城与订单
• 商品：/api/products 提供商品列表、详情、上架下架与日志；传承人作为卖家管理自身商品。
• 订单：/api/mall 用户下单与支付（模拟）、订单项与状态流转；/api/seller/orders 卖家查看与处理自己的订单。
4.9 积分体系
• 每日签到：/api/points/signin 记录连续签到与当前积分，支持规则化奖励。
• 行为奖励：/api/points/resource-view 等记录资源观看等行为积分；/api/points/info、/api/points/log 查询积分与奖励历史。
4.10 后台运营管理
• 仪表盘：前端 /admin/dashboard 汇总关键指标与待办。
• 内容治理：资源库、师承关系、评论与帖子审核、商城与票务的合规管理。
• 站点管理：首页内容配置、用户管理（仅管理员角色可见）。
• 系统设置与日志：提供基础设置与审计能力。
五、接口与前端路由示例
• 前端路由：/（门户）、/profile（用户中心）、/inheritor/apply（传承人申请）、/admin/*（后台）。
• 后端主要接口命名：/api/auth、/api/users、/api/inheritor、/api/master/apprentice、/api/teaching、/api/resources、/api/admin/*、/api/community/*、/api/events、/api/ticket、/api/products、/api/mall、/api/seller/orders、/api/points。
六、数据模型与数据库
• 用户表 sys_user：含用户名、加密密码、真实姓名、联系方式、头像、角色、积分、连续签到天数、状态与创建时间。
• 传承人资料 inheritor_profile：等级、流派、艺术经历、获奖情况、审核状态与关联用户。
• 资源、活动、票务、商品、订单、社区帖子与评论、积分日志等表详见 liuqin_opera_db.sql。
七、配置与部署
• 配置：application.yml 设置数据库、JWT 密钥与过期时间、日志级别、文件上传限制等；前端 vite.config.js 配置 /api 与 /files 代理到后端 8081。
• 启动：后端使用 Spring Boot Maven 插件指定主类 com.lqx.opera.LqxOperaApplication；前端使用 npm run dev 启动 Vite 开发服务器。
• 安全建议：生产环境请更换强随机 JWT 密钥、开启 HTTPS、最小化暴露接口并强化审计与告警。
八、前后端交互约定
• 统一返回：Result<T> 封装 code、message、data；错误码与文案前后端一致。
• 鉴权：Authorization: Bearer <token>；拦截器将 userId/username/role 注入，前端基于角色控制路由与菜单。
• 文件：后端开启 multipart 上传，前端通过 /files 代理访问静态文件与上传结果。
九、典型业务流程
• 传承人入驻：用户提交资料 → 后台审核通过 → 获得角色 1 权限 → 可发布教学任务、商品、活动。
• 徒弟拜师与教学：学生申请拜师 → 师父审核通过 → 师父发布任务 → 学生提交作业 → 师父点评 → 形成作品与资源。
• 商城与订单：用户下单 → 生成订单与订单项 → 卖家发货/处理 → 订单完成；全程支持查看与售后流程。
• 活动与票务：创建演出活动 → 配置票类与库存 → 用户购票 → 核验与入场。
• 积分运营：用户每日签到、观看资源等获得积分 → 积分用于激励与等级体系（可扩展）。
十、前端页面结构简述
• ClientLayout：门户站点页头导航、主页模块入口、社区与用户页；配色与中国风样式统一。
• AdminLayout：后台侧边导航（非遗业务、商业运营、内容治理、站点管理、系统管理），基于角色显示菜单。
• 视图页：InheritorApply、UserProfile、CommunityIndex/Detail、AuditorDashboard、AdminInheritorReview、AdminResourceAudit、Tickets、Mall、SellerOrders 等。
十一、扩展与二次开发建议
• 模块化：各模块的控制器与服务已相对解耦，可按领域进行扩展（如积分商城、直播课堂、评价体系）。
• 审核流：可加入工作流引擎（如状态机或审批流）提升运营效率与可追溯性。
• 安全与合规：完善敏感词过滤、版权标识、用户数据保护与日志审计；生产环境关闭调试日志。
以上内容覆盖本系统的核心功能、架构与使用说明，供产品、运营与技术参考。