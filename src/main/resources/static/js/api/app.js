// 순환 참조 제거
export const api = {
    brand: null,
    category: null,
    modal: null,
    utils : null
};

// 나중에 초기화 함수를 통해 모듈들을 연결
import {brandAPI} from './brand/brandAPI.js';
import {categoryAPI} from "./category/categoryAPI.js";
import {modal} from "../modal/modal.js";
import {utils} from "../utils/utils";

// 모듈 연결
api.brand = brandAPI;
api.category = categoryAPI;
api.modal = modal;
api.utils = utils;