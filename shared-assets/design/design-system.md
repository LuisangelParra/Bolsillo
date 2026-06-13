# Bolsillo — Design System (shared source of truth)

Platform-neutral visual contract for **both** native apps (Android / Jetpack Compose, iOS / SwiftUI). No platform code here.

- **Canonical visual reference:** [`docs/design/Bolsillo.dc.html`](../../docs/design/Bolsillo.dc.html) — keep it; pixels there win in any dispute.
- **Machine-readable tokens:** [`tokens.json`](./tokens.json) — both apps map these into native resources (Compose `Theme`/`Color`, SwiftUI `Color`/`Font` assets). Do **not** hardcode hex in UI code.
- **Rule:** if the two apps diverge visually, fix it here first, then in both. UI text is never hardcoded — strings come from `shared-assets/i18n` (es default, en) via keys (constitution Art. VII).

Light values are extracted from the mock. The mock ships **light only**; dark values in `tokens.json` are a derived proposal — refine when a dark mockup exists.

---

## 1. Foundations

### 1.1 Color (semantic)
Use role names, never raw hex. Full light/dark table in `tokens.json → color`.

| Role | Light | Use |
|---|---|---|
| `background` | `#F4F3F6` | app canvas behind cards |
| `surface` | `#FFFFFF` | cards, sheets content, rows |
| `surfaceAlt` | `#F7F6FA` | nested fill inside a card |
| `surfaceInverse` | `#16161B` | dark pills (insight, toast), receipt scrim |
| `primary` | `#7C5CF0` | brand violet — actions, active states, caret |
| `primaryContainer` | `#EDE7FE` | violet tint chips/keypad accents |
| `onPrimary` | `#FFFFFF` | text/icon on primary |
| `onPrimaryContainer` | `#6B4FD8` | text on violet tint |
| `textPrimary` | `#16161C` | headings, amounts, body emphasis |
| `textSecondary` | `#56565F` | secondary buttons/labels |
| `textMuted` | `#9A9AA4` | captions, hints, inactive |
| `textDisabled` | `#C4C3CC` | placeholder amount, disabled |
| `outline` | `#E7E6EC` | segmented control bg, hairline borders |
| `divider` | `#F2F1F5` | row separators |
| `track` | `#F0EFF4` | progress/bar backgrounds |
| `success` | `#16B364` (container `#DCF5E9`) | income, confident AI, positive delta |
| `danger` | `#F0425A` (container `#FDE6EC`) | expense amounts, overspend, negative delta |
| `warning` | `#E0852B` (container `#FBEFD6`, border `#F0DBB0`) | "to confirm", 80–100% budget |
| `info` | `#3B82F6` (container `#E3EFFE`) | informational, transport-family |
| `amountPositive` / `amountNegative` | `#16B364` / `#F0425A` | signed money display |
| `notificationDot` | `#FF5470` | unread badge |

**Brand gradients** (`tokens.json → gradient`): `hero` (home header), `primary`/`fab` (save button, FAB), `avatar`.

**Money color rule:** positive/income → `amountPositive` with `+` prefix; negative/expense → `amountNegative` with `-` prefix. Always tabular figures.

### 1.2 Category colors
13 semantic categories, each an **icon foreground** + **container fill** pair (`tokens.json → category`). Icon tiles use the container as background and the fg for the stroke icon. Income reuses `mercado`/`ingreso` green. Donut/breakdown charts pull from `chartPalette`.

| id | fg | container (light) |
|---|---|---|
| cafe | `#E0A12B` | `#FBEFD6` |
| mercado | `#16B364` | `#DCF5E9` |
| restaurantes | `#F2843B` | `#FCEBDD` |
| transporte | `#3B82F6` | `#E3EFFE` |
| ropa | `#7C5CF0` | `#EDE7FE` |
| ocio | `#2BA4D9` | `#DEF1FB` |
| salud | `#F0425A` | `#FDE6EC` |
| vivienda | `#12A89A` | `#DAF3F0` |
| servicios | `#7C5CF0` | `#EDE7FE` |
| educacion | `#3B82F6` | `#E3EFFE` |
| viajes | `#2BA4D9` | `#DEF1FB` |
| otros | `#75757F` | `#ECECEF` |
| ingreso | `#16B364` | `#DCF5E9` |

### 1.3 Typography
Family **Plus Jakarta Sans** (fallback system sans). Weights 400/500/600/700/800. Roles in `tokens.json → typography.role`; key ones:

| Role | Size/Weight | Use |
|---|---|---|
| `displayBalance` | 46 / 800, −1 ls, tabular | home total balance |
| `displayAmount` | 52 / 800, −1.5 ls, tabular | record amount entry |
| `moneyXL` / `moneyL` | 27 / 26 / 800, tabular | goal amount, donut center |
| `keypadDigit` | 23 / 700 | keypad keys |
| `titleXL` / `titleL` / `titleM` | 21 / 18 / 17, 800–700 | screen / sheet / section titles |
| `button` | 16 / 800 | primary buttons |
| `bodyStrong` / `amountRow` | 15.5 / 700–800 | row title / row amount (tabular) |
| `body` / `bodyValue` | 14.5 / 600–700 | body text / values |
| `label` / `labelSmall` / `caption` | 13 / 12.5 / 12 | captions, hints |
| `badge` | 11.5 / 700 | confidence + pill chips |
| `navLabel` | 11 / 500–700 | bottom-nav labels |
| `overline` | 12.5 / 700, uppercase, .5 ls | settings group headers |

**Tabular figures are mandatory** on every monetary number so digits don't jitter.

### 1.4 Spacing
px scale (`tokens.json → spacing`): `xxs 2 · xs 4 · s 6 · sm 8 · m 10 · base 12 · ml 14 · l 16 · xl 18 · xxl 20 · 3xl 22 · 4xl 24 · 5xl 26 · 6xl 30`. Screen horizontal padding **18** (content) / **20** (sheets). Card inner padding **14–18**. Inter-card gap **9–12**.

### 1.5 Radii
`iconTileSm 11 · iconTile 13 · iconTileLg 17 · control/chip 16 · card 18 · cardLarge 20 · cardXL 22 · nav 26 · sheet 30 · frame 46 · full 9999`. Pills/badges/round buttons = `full`. Bottom sheets = `sheet` (top corners only).

### 1.6 Elevation / shadows
Ambient shadow is **dark-violet** `rgba(28,20,60,a)` in light mode (use `rgba(0,0,0,a)` in dark). Levels (`tokens.json → elevation`): `e1` controls → `e2/e2b` list cards → `e3` raised cards → `e4` prominent (goal) → `nav` floating bar → `fab`/`buttonPrimary` violet-tinted glows → `toast`. Keep cards low and soft; only FAB/save carry a colored glow.

### 1.7 Iconography
Line icons, 24×24 viewbox, stroke width **2** (2.2–2.8 for emphasis), round caps/joins, `fill:none`, `stroke=currentColor`. Each app keeps its own icon set but must match these names/shapes: coffee, cart, utensils, car, bag, tv, heart, home, zap, cap, umbrella, tag, banknote, wallet, card, bell, settings, plus, spark, camera, type, mic, back, chevron(d/r), check, x, bars, pie, target, dots, search, lock, layers, info, share, arrow, repeat, download, shield.

### 1.8 Motion
`tokens.json → motion`. Sheet present 320ms `cubic-bezier(.22,1,.36,1)`; fade 200ms; toast rise 300ms; result "pop" 250ms; caret blink 1100ms. **Undo toast visible 5000ms** (spec §FR6 = ~5 s; mock used 4500 — 5000 is canonical). Learning toast 2400ms. Recording shows **no loading spinner** (constitution Art. IV).

---

## 2. Components

Each component: **structure → states → tokens**. Components consume semantic tokens only.

### 2.1 Amount keypad  *(record sheet — the core of feature 001)*
- **Structure:** Amount display row at top — `$` symbol (`moneyL`) + value (`displayAmount`, tabular) + blinking caret bar (`caret`, 3px). Currency caption below (`label`, `textMuted`, e.g. "COP · Peso colombiano"). A violet hint strip (`primaryContainer` bg, `onPrimaryContainer` text, `control` radius) reads "category & account ready — type the amount and save". Grid **3×4** of keys: `1–9`, `000`, `0`, `⌫`. Each key: `surface` bg, `keypadDigit`, radius `control`, shadow `key`.
- **States:**
  - *empty* — value `0` in `textDisabled`; caret `textDisabled`.
  - *typing* — value in `textPrimary` (or `success` when type=income); caret `primary`.
  - *income mode* — value tinted `amountPositive`.
  - key *pressed* — brief scale/opacity feedback.
- **Behavior:** integer minor-unit entry only (no decimal/float). `000` appends thousands; `⌫` removes last digit. Auto-focus on sheet open; keyboard never covers it (custom keypad, not the system IME).
- **Tokens:** `surface`, `primary`, `primaryContainer`, `textPrimary/Disabled/Muted`, type role `displayAmount`/`keypadDigit`, radius `control`, elevation `key`, motion `caretBlink`.

### 2.2 Transaction row  *(home feed)*
- **Structure:** Horizontal card (`surface`, radius `card`, padding 13–14, shadow `e2`). Left: category icon tile (44×44, radius `iconTile`, category container bg + fg icon; income uses banknote + success). Middle: title (`bodyStrong`) with optional "Por confirmar" badge; sub-line = tag/recurring icon + merchant (`labelSmall`, `textMuted`). Right: signed amount (`amountRow`, `amountPositive`/`amountNegative`) over account name (`caption`, `textMuted`).
- **States:** *default*; *to-confirm* (amber badge `warning`/`warningContainer`, see 2.3); *income* (green tile + `+` amount); *pressed* (row highlight). Grouped under a **day header**: weekday (`label`, `textMuted`, capitalized) + day total.
- **Tokens:** `surface`, category `container`/`fg`, `amountPositive/Negative`, `textPrimary/Muted`, `warningContainer`/`warning`, radius `card`/`iconTile`, elevation `e2`.

### 2.3 Category chip with confidence state  *(record sheet)*
- **Structure:** Pill/card button (`surface`, radius `control`, 1.5px border). Left: small category tile (38×38, radius `iconTileSm`). Stacked label "Categoría" (`caption`, `textMuted`) + category name (`bodyValue`). Trailing chevron. Paired beside it: an **account chip** (same shape, wallet icon). Below the pair: **AI confidence line** = a badge (icon + label, `badge`) + "Sugerido por IA · aprende de ti" caption.
- **Confidence states** (`tokens.json → confidence`, threshold = **0.75**, constitution Art. V):
  - *waiting* (`conf ≤ 0`): spark icon, `textMuted` on `fill`; chip border `surface`.
  - *to-confirm* (`0 < conf < 0.75`): info icon, `warning` on `warningContainer`; **chip border `warningBorder`**; label key `record.ai.toConfirm`.
  - *confident* (`conf ≥ 0.75`): check icon, `success` on `successContainer`; chip border `surface`; label `IA NN%`.
  - *user-picked*: treated as confident (border `surface`, success badge).
- **Rule:** AI never blocks saving; confidence is shown but unobtrusive. Tapping the chip opens the category picker (alternatives w/ confidence bars + full grid).
- **Tokens:** `surface`, category `container`/`fg`, `warning(+Container,+Border)`, `success(+Container)`, `fill`, `textMuted`, radius `control`/`iconTileSm`, type `badge`/`bodyValue`/`caption`.

### 2.4 Account card / chip
- **Two forms:**
  1. **Account chip** (in record sheet, mirrors the category chip): tile + "Cuenta" label + account name (e.g. "Efectivo") + chevron; default tile uses `successContainer` + wallet icon.
  2. **Money summary card** (home "Tu dinero"): `surface` card, radius `cardLarge`, shadow `e3`. Icon tile (38×38, radius `iconTile`) top-left, then label (`label`, `textMuted`) and value (`titleM`-ish 21/800, tabular). Income card uses `successContainer`/`success`; expense card uses `dangerContainer`/`danger`.
- **States:** default; selected (in a picker → primary border); archived (muted, hidden from pickers per E3).
- **Tokens:** `surface`, `successContainer`/`dangerContainer`, `textPrimary/Muted`, radius `cardLarge`/`iconTile`, elevation `e3`.

### 2.5 Budget bar  *(plan screen)*
- **Two variants:**
  1. **Linear progress** (goal/breakdown): track (`track`, height 7–11, radius `full`), fill in category `fg` (or a striped "remaining" segment for goals). Caption row: left label (`labelSmall`, `textMuted`), right value (`bodyValue`, `textPrimary`).
  2. **Ring gauge** (budget list row): 46×46 conic ring, percent in center (`badge`). Color thresholds: `< 80%` → `success`, `80–100%` → `warning`, `> 100%` → `danger`. Row also shows category tile + name + "spent de cap".
- **States:** under (green), nearing (amber, 80–100%), over (red, >100% — overspend). Pace-alert styling reuses `warning`/`danger`.
- **Tokens:** `track`, category `fg`, `success`/`warning`/`danger`, `surface`, radius `full`/`card`/`iconTile`, type `badge`/`bodyValue`/`labelSmall`.

### 2.6 Bottom navigation
- **Structure:** Floating bar, `surface`, radius `nav`, inset 14 sides / 20 bottom, height 64, shadow `nav`. 4 tabs: Inicio, Reporte, Plan, Ajustes — icon + `navLabel`. A center **gap** is reserved for the FAB (one nav slot left empty / spacer). A top scrim gradient (`navScrim`) fades content above the bar.
- **States:** *active* — icon `primary` (stroke 2.3), label `textPrimary` weight 700; *inactive* — icon `#A9A9B2`/`textMuted`, label weight 500.
- **Tokens:** `surface`, `primary`, `textPrimary/Muted`, radius `nav`, elevation `nav`, type `navLabel`, gradient `navScrim`.

### 2.7 FAB / quick-add
- **Structure:** Circular 62px button centered over the nav, `insetBottom` 54, `gradient.fab`, 4px border in `background` (cut-out effect), white `plus` icon (26, stroke 2.8), shadow `fab`.
- **Behavior:** primary entry to the **record sheet** (opens at keypad mode, type=expense, amount 0). This is the path to the ≤3-tap / ≤5-s recording flow (feature 001).
- **States:** default; pressed (scale ~0.96); the same gradient/glow is reused by the sheet's **Save** button.
- **Tokens:** `gradient.fab`, `background` (border), `onPrimary`, radius `full`, elevation `fab`.

---

## 3. Supporting components (seen in the mock)

- **Hero balance header** — `gradient.hero`, rounded bottom (`cardLarge`), avatar + month switcher + bell (with `notificationDot`); centered "Saldo total" + `displayBalance` + delta pill. Status bar text switches to white over the hero.
- **Insight pill** — full-width `surfaceInverse` button, radius `card`; spark tile + white text + `primaryAccent` "Ver Pro".
- **Record sheet** — bottom sheet (`background`, radius `sheet`, present 320ms). Grab handle (`sheetHandle`). Header title + close (X) button (`fill` circle). Contains: **type toggle** (Gasto/Ingreso/Transferencia segmented), amount keypad, category+account chips, AI line, **mode tabs** (Teclado/Texto/Recibo), input area, Save button.
- **Segmented control** — `outline` track, radius `control`, selected thumb `surface` + shadow `segmentedThumb`, selected text `textPrimary` / unselected `textMuted`. Used for type toggle and report Gastos/Ingresos.
- **Mode tabs** — pill buttons; active `primary`/`onPrimary`, inactive `fill`/`textSecondary`. (Texto/Recibo belong to deferred specs 007 — keep visual slot.)
- **Category picker** — bottom sheet; "Sugerencia de IA" box (alternatives list, each with a confidence **bar** in category `fg` + `%`, top item highlighted/checked) over a 4-col grid of all categories (54px tiles, selected = category `fg` border).
- **Toasts** — `surfaceInverse`, radius `full`, shadow `toast`, rise 300ms. **Undo toast**: green check + message + "Deshacer" action (in `primaryAccent`), visible **5 s**. **Learning toast**: spark + "Aprenderé de tu elección", 2.4 s.
- **Donut chart** — conic from `chartPalette`, center hole shows total (`moneyL`); slice badge in `surfaceInverse`.
- **Breakdown row** — category tile + name + amount, delta pill (`success`/`danger` container), linear progress in category `fg`.
- **Goal card** — `surface`, radius `cardXL`, shadow `e4`; target tile, amount/of-target, striped progress (solid `restaurantes` orange done + diagonal stripes remaining), pace alert strip in `warning`.
- **Settings row** — grouped list (`surface`, radius `card`); 36px icon tile + label (`body`) + value (`labelSmall`, `textMuted`) + chevron; group header in `overline`. Offline/privacy status uses `success` dot.

---

## 4. Cross-platform parity rules
1. **Tokens are the contract.** Both apps generate theme resources from `tokens.json`; no literal colors/sizes in components.
2. **Money formatting** follows the active locale + each currency's `decimalDigits`; always tabular figures; sign + color per §1.1.
3. **Strings** via i18n keys (es default, en) — no hardcoded UI text; verify no truncation in either language.
4. **Confidence threshold** = `ExpenseClassifier.DEFAULT_THRESHOLD` (0.75). The chip states in §2.3 key off it, not a hardcoded number.
5. **Recording is spinner-free** and reaches Save in ≤3 taps / ≤5 s (constitution Art. IV).
6. **Dark mode** values are provisional until a dark mockup lands; brand violet and category fg stay constant, surfaces/containers deepen.
7. **Divergence is a design-system defect** — fix this file + `tokens.json` first, then both apps.
