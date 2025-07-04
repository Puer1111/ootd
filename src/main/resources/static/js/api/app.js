// 순환 참조 제거
export const api = {
    brand: null,
    category: null,
    modal: null,
    utils : null,
    colors : null,
    // product : null,
    size: null,
    coupon: null
};

// 나중에 초기화 함수를 통해 모듈들을 연결
import {brandAPI} from './brand/brandAPI.js';
import {categoryAPI} from "./category/categoryAPI.js";
import {modal} from "../modal/modal.js";
import {utils} from "../utils/utils.js";
import {colors} from "./color/colorAPI.js";
// import {product} from "./product/productAPI.js";
import {size} from "./size/sizeAPI.js";
import {coupon} from "./coupon/couponAPI.js";
// 모듈 연결
api.brand = brandAPI;
api.category = categoryAPI;
api.modal = modal;
api.utils = utils;
api.colors = colors;
// api.product = product;
api.size = size;
api.coupon = coupon;