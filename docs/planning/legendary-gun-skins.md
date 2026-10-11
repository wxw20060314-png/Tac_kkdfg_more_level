# 传说枪自定义涂装 — 规划备忘

> 状态：外观已复原为 TACZ 原版 display；自定义 png 暂不进模组 / 不进 `tacz_default_gun`。  
> 枪数据、index id、传说技能不变。

## 当前（已做）

| 枪 | index | display（现） | 显示名 |
|----|--------|---------------|--------|
| 歼灭者 HK416D | `tacz:hk416d_annihilator` | `tacz:hk416d_display` | 不变 |
| 传说加特林 | `tacz:minigun_legendary` | `tacz:minigun_display` | 加特林战争机器 |

已删除（勿再写回官方默认枪包）：

- `run/tacz/tacz_default_gun/assets/tacz/display/guns/hk416d_annihilator_display.json`
- `run/tacz/tacz_default_gun/assets/tacz/display/guns/minigun_legendary_display.json`

## 贴图落点（项目内，不打进 jar）

把 png 放到 `docs/planning/gun-skins/`，按 TACZ 路径分子目录。仓库里目前没有这些文件，有图后再丢进来。

| 落点 | 以后枪包内路径 | ResourceLocation |
|------|----------------|------------------|
| `docs/planning/gun-skins/uv/hk416a5_annihilator.png` | `assets/tacz/textures/gun/uv/` | `tacz:gun/uv/hk416a5_annihilator` |
| `docs/planning/gun-skins/lod/hk416a5_annihilator.png` | `assets/tacz/textures/gun/lod/` | `tacz:gun/lod/hk416a5_annihilator` |
| `docs/planning/gun-skins/slot/hk416a5_annihilator.png` | `assets/tacz/textures/gun/slot/` | `tacz:gun/slot/hk416a5_annihilator` |
| `docs/planning/gun-skins/uv/minigun_legendary.png` | `assets/tacz/textures/gun/uv/` | `tacz:gun/uv/minigun_legendary` |
| `docs/planning/gun-skins/lod/minigun_legendary.png` | `assets/tacz/textures/gun/lod/` | `tacz:gun/lod/minigun_legendary` |
| `docs/planning/gun-skins/slot/minigun_legendary.png` | `assets/tacz/textures/gun/slot/` | `tacz:gun/slot/minigun_legendary` |

HUD 继续用原版：`tacz:gun/hud/hk416a5`、`tacz:gun/hud/minigun`。

## 以后若做独立枪包（不做进 morelevel.jar）

1. 新建 `tacz/` 下独立包（`gunpack.meta.json` 的 `namespace` 用 `tacz`），**不要**改 `tacz_default_gun`。
2. display 从原版 `hk416d_display` / `minigun_display` 复制，只改 `texture` / `lod.texture` / `slot` 为上表 id。
3. index 再改回 `tacz:hk416d_annihilator_display`、`tacz:minigun_legendary_display`。
