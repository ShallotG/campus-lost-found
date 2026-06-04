package com.campus.lostfound.service;

import com.campus.lostfound.dto.request.SearchRequest;
import com.campus.lostfound.dto.response.SearchResultResponse;

public interface SearchService {

    /**
     * 自然语言语义检索
     * 调用DeepSeek Embedding + 余弦相似度排序
     */
    SearchResultResponse search(SearchRequest request);
}
