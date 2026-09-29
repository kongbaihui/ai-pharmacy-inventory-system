-- ----------------------------------------------------------------------------
-- common 普通角色权限收敛
-- 允许：药品库存工作台（前端固定路由）、药品进销存管理、AI 前端界面
-- 禁止：系统管理、系统监控、系统工具及其他动态菜单
-- 前提：已执行 04_web_menu.sql；脚本可重复执行
-- ----------------------------------------------------------------------------
SET NAMES utf8mb4;

SET @commonRoleId = (
    SELECT role_id FROM sys_role
    WHERE role_key = 'common'
    ORDER BY role_id LIMIT 1
);

DROP TEMPORARY TABLE IF EXISTS tmp_common_allowed_menu;
CREATE TEMPORARY TABLE tmp_common_allowed_menu (
    menu_id BIGINT NOT NULL PRIMARY KEY
);

INSERT INTO tmp_common_allowed_menu (menu_id)
WITH RECURSIVE allowed_menu AS (
    SELECT menu_id
    FROM sys_menu
    WHERE (parent_id = 0 AND menu_name = '药品进销存管理' AND menu_type = 'M')
       OR component = 'system/AIChat/index'

    UNION ALL

    SELECT child.menu_id
    FROM sys_menu child
    INNER JOIN allowed_menu parent ON child.parent_id = parent.menu_id
)
SELECT menu_id FROM allowed_menu;

DELETE FROM sys_role_menu
WHERE role_id = @commonRoleId;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT @commonRoleId, menu_id
FROM tmp_common_allowed_menu
WHERE @commonRoleId IS NOT NULL;

DROP TEMPORARY TABLE tmp_common_allowed_menu;
