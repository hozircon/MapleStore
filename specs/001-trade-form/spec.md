# Feature Specification: Lightweight Trading Showcase

**Feature Branch**: `001-trade-form`
**Created**: March 18, 2026
**Status**: Draft
**Input**: User description: "輕量化交易展示站 - 建立具備搜尋與篩選功能的商品展示頁，方便管理員發布個人庫存，讓買家能快速定位所需商品，取代舊版楓之谷私服社群中低效率的截圖分享方式。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Buyer Searches and Filters Items (Priority: P1)

A buyer visits the showcase website and wants to find a specific item. They type a keyword into the search box, select a category, and optionally narrow down results with sub-filters (e.g., job class or stat thresholds for equipment). They click the Search button and receive a relevant, sorted list of available items.

**Why this priority**: This is the core value proposition of the site — without search and filter, the showcase offers no improvement over screenshot sharing in community channels.

**Independent Test**: With a loaded dataset, enter a keyword, select the 裝備 (Equipment) category, set ATK ≥ 10, and click Search. Verify that only in-stock equipment matching the name and stat threshold appears, sorted by currency priority.

**Acceptance Scenarios**:

1. **Given** a buyer enters a partial item name, **When** they click Search, **Then** results show only items whose name contains the entered text (fuzzy/partial match).
2. **Given** a buyer selects category 裝備 and sets STR ≥ 30, **When** they click Search, **Then** only equipment with STR ≥ 30 appears in results.
3. **Given** a buyer selects only the CS currency checkbox, **When** they click Search, **Then** only CS-priced items appear in results.
4. **Given** no items match the current filter, **When** the buyer clicks Search, **Then** the result area shows a clear empty-state message instead of a blank page.

---

### User Story 2 - Buyer Views Results Sorted by Currency Priority (Priority: P2)

A buyer sees search results sorted by trading convention — MESO-priced items first, CS second, WS third — with each group sorted by price ascending. This allows buyers to quickly identify offers in their preferred currency.

**Why this priority**: Sorting by the established community trading hierarchy is essential for usability; buyers lose time scanning if results are unordered.

**Independent Test**: Load a result set containing at least one MESO, one CS, and one WS listing. Verify that all MESO items appear before all CS items, which appear before all WS items, and each group is sorted by price from lowest to highest.

**Acceptance Scenarios**:

1. **Given** results contain MESO, CS, and WS items, **When** displayed, **Then** items appear grouped: MESO first, CS second, WS third.
2. **Given** multiple items share the same currency type, **When** displayed, **Then** they are ordered by price from lowest to highest within the group.

---

### User Story 3 - Buyer Reads Equipment vs. General Item Details (Priority: P2)

A buyer can easily distinguish between equipment listings and general item listings. Equipment rows display a full stat breakdown (STR, DEX, INT, LUK, ATK, MATK, scroll slots remaining), while general item rows show quantity and subcategory. Both templates include item icon, name, price with currency indicator, and seller info.

**Why this priority**: Equipment purchase decisions depend on stat comparison; without this differentiation, buyers cannot meaningfully evaluate equipment value.

**Independent Test**: Load a mixed result set with both equipment and general items. Verify equipment rows display stat columns and general item rows display quantity/subcategory columns.

**Acceptance Scenarios**:

1. **Given** a result contains equipment, **When** displayed, **Then** each equipment row shows: item icon, name, STR, DEX, INT, LUK, ATK, MATK, scroll slots remaining, price with currency icon, and seller name and location.
2. **Given** a result contains general items (consumable, decorative, other), **When** displayed, **Then** each row shows: index, item icon, name, quantity, price with currency icon, and seller name and location.
3. **Given** an equipment item has no value for a stat (e.g., no STR bonus), **When** displayed, **Then** the stat field shows 0 rather than a blank or error.

---

### User Story 4 - Buyer Copies Trade Code to Contact Seller (Priority: P3)

After finding a desired item, a buyer clicks a "Copy Trade Code" button on the listing. A pre-formatted in-game private message (including item name, price, and seller's in-game location) is placed on the clipboard. The buyer pastes it in-game to contact the seller directly.

**Why this priority**: Removes manual note-taking friction between discovery and trade completion, directly enabling conversions.

**Independent Test**: Click "Copy Trade Code" on any listing. Verify the clipboard contains a message formatted with item name, price, and seller location (e.g., CH1 FM03). Verify a brief confirmation feedback appears.

**Acceptance Scenarios**:

1. **Given** a buyer has found a desired item, **When** they click "Copy Trade Code", **Then** the clipboard receives a pre-formatted message containing item name, price, currency, and seller in-game location.
2. **Given** the copy action completes, **Then** a brief feedback (e.g., tooltip "Copied!") is shown to confirm success.

---

### User Story 5 - Admin Manages Inventory Status (Priority: P4)

An admin can access a management area to mark individual items as sold-out, and to bulk-remove all sold-out items in one action. This keeps the public listing accurate without requiring item-by-item deletion.

**Why this priority**: Stale sold-out listings erode buyer trust and waste buyers' time; regular inventory cleanup is mandatory for the showcase to remain useful.

**Independent Test**: Mark one item as sold-out and confirm it no longer appears in buyer search results. Use the bulk-remove function and verify all sold-out items are removed in one step.

**Acceptance Scenarios**:

1. **Given** an admin marks an item as sold-out, **When** a buyer searches, **Then** the sold-out item does not appear in results.
2. **Given** multiple items are marked as sold-out, **When** the admin uses bulk-remove, **Then** all sold-out items are removed in a single operation.
3. **Given** an admin updates an item's price, quantity, or location, **When** a buyer searches, **Then** the updated information is reflected immediately.

---

### Edge Cases

- What happens when a buyer submits a search with no filters and no keyword? → All in-stock items are returned, sorted by currency priority.
- What happens when equipment has missing stat data for a given field? → The field displays 0; the item remains visible in results.
- What happens when the admin marks the last item in a category as sold-out? → The category filter remains visible; search returns an empty-state message.
- What happens if the buyer's browser does not support clipboard access? → "Copy Trade Code" gracefully fails and displays the trade code text in a selectable field instead.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a keyword search input that matches item names using partial (fuzzy) matching.
- **FR-002**: System MUST support filtering by top-level category: 裝備 (Equipment), 消耗 (Consumable), 裝飾 (Decorative), and 其他 (Other).
- **FR-003**: System MUST support filtering by currency type (MESO, CS, WS) via multi-select checkboxes, with all types selected by default.
- **FR-004**: When the 裝備 category is selected, the system MUST display advanced filters: job class selector, equipment slot selector, and numeric threshold inputs for STR, DEX, INT, LUK, ATK, and MATK.
- **FR-005**: When a non-equipment category is selected, the system MUST display a subcategory dropdown appropriate to that category (e.g., scroll, material, ore for consumables).
- **FR-006**: Search MUST be manually triggered via a dedicated Search button; results MUST NOT auto-refresh on filter changes.
- **FR-007**: System MUST display equipment search results using a stats-focused row template that includes: item icon, name, STR, DEX, INT, LUK, ATK, MATK, scroll slots remaining, price with currency icon, seller name, and seller in-game location.
- **FR-008**: System MUST display general item search results using a table row template that includes: row index, item icon, name, quantity, price with currency icon, seller name, and seller in-game location.
- **FR-009**: Search results MUST always be sorted by currency priority (MESO → CS → WS) then by price ascending within each currency group.
- **FR-010**: Every listing MUST include a "Copy Trade Code" button that places a pre-formatted in-game contact message on the clipboard.
- **FR-011**: "Copy Trade Code" MUST fall back to displaying the trade code as selectable text if clipboard access is unavailable.
- **FR-012**: Admin MUST be able to individually mark any item as in-stock or sold-out.
- **FR-013**: Admin MUST be able to bulk-remove all sold-out items in a single operation.
- **FR-014**: Only in-stock items MUST be visible to buyers in search and filter results.

### Key Entities

- **Item**: Represents a product listed for trade. Core attributes: game item ID, display name, top-level category, subcategory, equipment stats (STR, DEX, INT, LUK, ATK, MATK, scroll slots remaining), currency type (MESO / CS / WS), price value, quantity, in-game seller location (channel and Free Market slot), seller character name, availability status (in-stock / sold-out), and a reserved "featured" flag for future promotional use.

### Assumptions

- Admin access control and login are out of scope for this feature; accessing the admin area is treated as trusted in this iteration.
- Item icons are sourced from static game asset files and resolved by game item ID; no image upload functionality is required.
- Buyers are read-only; item creation and editing are admin-only operations.
- Advanced equipment stat filters use AND logic — all specified conditions must be satisfied simultaneously.
- "In-game location" is a human-readable string entered by the admin (e.g., "CH1 FM03") rather than a structured field.

### Out of Scope

- Real-time chat or in-app messaging between buyers and sellers.
- Payment processing or virtual currency transactions within the site.
- Buyer account registration or login.
- Automated stock synchronization with any external game API.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A buyer can locate a specific item by name within 30 seconds of arriving on the page, using search and filters.
- **SC-002**: Search results are displayed within 2 seconds of clicking the Search button under normal operating conditions.
- **SC-003**: 100% of listed items correctly display the currency type icon alongside the price value, with no missing or mismatched icons.
- **SC-004**: Search results consistently place all MESO-priced items before all CS-priced items, which appear before all WS-priced items, in every query.
- **SC-005**: An admin can mark an item as sold-out and confirm it has disappeared from buyer results within 1 minute.
- **SC-006**: The bulk-remove operation removes all sold-out items in a single action, requiring no more interactions than a regular single-item removal.