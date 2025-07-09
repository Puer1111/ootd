# Project Problems and Solutions Log

## 2025년 7월 8일

### 1. `adminProduct.js`의 `SyntaxError: Unexpected token '<', "<DOCTYPE "... is not valid JSON` 에러

**문제:**
`adminProduct.js`에서 `/admin/product` 엔드포인트로 API 호출 시, JSON 응답을 기대했으나 HTML (아마도 에러 페이지)을 받아 `SyntaxError` 발생.

**원인:**
`AdminController.java`의 `/admin/product` GET 요청 핸들러가 뷰(HTML)를 반환하도록 되어 있었음. 실제 상품 데이터를 JSON으로 반환하는 엔드포인트는 `/admin/select/product`였음.

**해결책:**
`adminProduct.js`의 `loadProducts()` 함수에서 API 호출 URL을 `/admin/product`에서 `/admin/select/product`로 변경.

### 2. `adminProduct.js`의 `handleEdit` 함수에서 `price` 및 `inventory` 필드 문제

**문제:**
- `handleEdit` 함수에서 상품 수정 시 `price` 필드가 제대로 채워지지 않음.
- `sizesContainer` 내의 `option.inventory` 값이 표시되지 않음.
- `price` 필드는 각 옵션(`size-item`)에 연결되어야 함.

**원인:**
- 초기에는 `price`가 상품 전체의 가격으로 오해하여 `adminProduct.html`에 별도의 `price` 입력 필드를 추가하려 했으나, 사용자 피드백을 통해 각 옵션별 가격임을 확인.
- `AdminProductDTO.java`의 `ProductOptionInfo`에 `price` 필드가 없었음.
- `adminProduct.js`의 `handleEdit` 함수에서 `size-item` 생성 시 `option.price`를 위한 입력 필드가 누락되었고, `option.inventory`의 `type`이 `text`로 되어 있어 숫자 입력에 부적합했음.

**해결책:**
1.  **백엔드 DTO 수정:**
    - `AdminProductDTO.java`의 `ProductOptionInfo` 클래스에 `private Integer price;` 필드 추가.
    - `ProductOptionDTO.java`에 `private List<Integer> price;` 필드 추가.
2.  **프론트엔드 JS 수정:**
    - `adminProduct.js`의 `handleEdit` 함수에서 `size-item` HTML 구조 내에 `option.price`를 위한 `<input type="number" name="productOption.price[]" value="${option.price}">` 필드 추가.
    - `option.inventory` 입력 필드의 `type`을 `text`에서 `number`로 변경.

### 3. 상품 목록 조회 시 하나의 상품만 조회되는 문제

**문제:**
`adminProduct.js`에서 상품 목록을 조회할 때, 데이터베이스에 여러 상품이 있음에도 불구하고 화면에 하나의 상품만 표시됨.

**원인:**
- `ProductRepository.java`의 `findAdminProducts()` 쿼리에서 `ProductOption` 및 `Colors` 테이블과의 `JOIN`이 `INNER JOIN`으로 되어 있어, 옵션 정보가 없는 상품이 결과에서 제외되었을 가능성.
- `AdminProductFlatDTO.java`의 `inventory` 필드가 `int` 타입이어서, `LEFT JOIN`으로 `NULL` 값이 반환될 때 `NullPointerException`이 발생하여 전체 상품 목록 빌드에 실패했을 가능성.

**해결책:**
1.  **쿼리 수정:**
    - `ProductRepository.java`의 `findAdminProducts()` 쿼리에서 `ProductOption` 및 `Colors` 테이블과의 `JOIN`을 `LEFT JOIN`으로 변경하여 모든 상품이 포함되도록 함.
2.  **DTO 필드 타입 수정:**
    - `AdminProductFlatDTO.java`의 `inventory` 필드 타입을 `int`에서 `Integer`로 변경하여 `NULL` 값을 안전하게 처리할 수 있도록 함.
    - `ProductServiceImpl.java`의 `getAdminProducts()` 메서드에서 `AdminProductDTO`를 빌드할 때 `AdminProductFlatDTO`의 `imageUrls`를 `AdminProductDTO`의 `imageUrls` 필드에 매핑하도록 수정.
    - `ProductServiceImpl.java`에서 `ProductOptionInfo`를 생성할 때 `flatDto.getInventory()`가 `null`일 경우 `0`으로 기본값 처리하도록 수정.

### 4. `adminProduct.js`에서 이미지 로드 문제

**문제:**
상품 목록에서 이미지가 로드되지 않음.

**원인:**
`ProductServiceImpl.java`의 `getAdminProducts()` 메서드에서 `AdminProductDTO`를 빌드할 때 `AdminProductFlatDTO`의 `imageUrls` 필드를 `AdminProductDTO`로 매핑하는 부분이 누락되었음.

**해결책:**
`ProductServiceImpl.java`의 `getAdminProducts()` 메서드에서 `firstOption.getImageUrls()`를 사용하여 `imageUrls`를 설정하도록 수정.
