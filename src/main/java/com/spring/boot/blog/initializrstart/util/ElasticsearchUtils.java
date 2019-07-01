//package com.spring.boot.blog.initializrstart.util;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.lang3.StringUtils;
//
//import org.elasticsearch.action.ActionFuture;
//import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
//import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
//import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
//import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
//import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
//import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
//import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
//import org.elasticsearch.action.bulk.*;
//import org.elasticsearch.action.delete.DeleteRequestBuilder;
//import org.elasticsearch.action.delete.DeleteResponse;
//import org.elasticsearch.action.get.GetRequestBuilder;
//import org.elasticsearch.action.get.GetResponse;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.action.index.IndexResponse;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.action.update.UpdateRequest;
//import org.elasticsearch.action.update.UpdateResponse;
//import org.elasticsearch.client.Requests;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.geo.GeoDistance;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.text.Text;
//import org.elasticsearch.common.unit.ByteSizeUnit;
//import org.elasticsearch.common.unit.ByteSizeValue;
//import org.elasticsearch.common.unit.DistanceUnit;
//import org.elasticsearch.common.unit.TimeValue;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.elasticsearch.index.query.*;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
//import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
//import org.elasticsearch.search.sort.SortBuilder;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.elasticsearch.search.sort.SortOrder;
//import org.elasticsearch.search.suggest.Suggest;
//import org.elasticsearch.search.suggest.SuggestBuilder;
//import org.elasticsearch.search.suggest.SuggestBuilders;
//import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
//import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import java.math.BigDecimal;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class ElasticsearchUtils {
//
//    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchUtils.class);
//
//    @Resource
//    private TransportClient transportClient;
//
//    private static TransportClient client;
//
//    private static BulkProcessor staticBulkProcessor = null;
//
//    /**
//     * @PostContruct是spring框架的注解 spring容器初始化的时候执行该方法
//     */
//    @PostConstruct
//    public void init() {
//        client = this.transportClient;
//    }
//
//    /**
//     * 判断索引是否存在
//     *
//     * @param index
//     * @return
//     */
//    public static boolean isIndexExist(String index) {
//        IndicesExistsResponse inExistsResponse = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
//        if (inExistsResponse.isExists()) {
//            logger.info("Index [" + index + "] is exist!");
//        } else {
//            logger.info("Index [" + index + "] is not exist!");
//        }
//        return inExistsResponse.isExists();
//    }
//
//    /**
//     * 判断inde下指定type是否存在
//     * @return
//     */
//    public boolean isTypeExist(String index, String type) {
//        return isIndexExist(index)
//                ? client.admin().indices().prepareTypesExists(index).setTypes(type).execute().actionGet().isExists()
//                : false;
//    }
//
//    /**
//     * 创建空白索引
//     *
//     * @param index
//     * @return
//     */
//    public static boolean createIndex(String index) {
//        if (isIndexExist(index)) {
//            logger.info("Index is exits!");
//            return false;
//        }
//        CreateIndexResponse indexResponse = client.admin().indices().prepareCreate(index).execute().actionGet();
//        logger.info("执行建立成功？" + indexResponse.isAcknowledged());
//        return indexResponse.isAcknowledged();
//    }
//
//    /**
//     * 一般在工作中关闭自动映射防止垃圾数据进入索引库,提前定义好索引库的字段信息当有非法的数据进来时会报错, 如果不知道字段信息则开启。
//     *
//     * 创建索引,手动指定索引的Mapping和Setting设置采用Json方式(当然还可以采用XContentBuilder方式,用法见下一个方法)
//     * @param index 索引名称
//     * @param setttings index的Setting
//     * @param mappings  index的Mapping
//     * @return
//     */
//    public static boolean createIndexBySettingAndMapping(String index, String type, String setttings, String mappings){
//        if(StringUtils.isEmpty(setttings)){
//            logger.info("Setttings can't null or \"\"");
//            return false;
//        }
//
//        if(StringUtils.isEmpty(mappings)){
//            logger.info("Mappings can't null or \"\"");
//            return false;
//        }
//
//        CreateIndexRequestBuilder cib = null;
//        if (isIndexExist(index)) {
//            //因为Mapping一旦创建将无法修改所以只能将原来的index删除后重新创建
//            deleteIndex(index);
//            cib = client.admin().indices().prepareCreate(index).setSettings(setttings, XContentType.JSON).addMapping(type, mappings, XContentType.JSON);
//        }else {
//            cib = client.admin().indices().prepareCreate(index).setSettings(setttings, XContentType.JSON).addMapping(type, mappings, XContentType.JSON);
//        }
//        //创建Mapping没有问题
//        //PutMappingRequest putMappingRequest = new PutMappingRequest(index).type(type).source(mappings, XContentType.JSON);
//        //PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();
//        //System.out.println(putMappingResponse.isAcknowledged());
//
//        CreateIndexResponse createMappingResponse = cib.execute().actionGet();
//        if(createMappingResponse.isAcknowledged()){
//            logger.info("mapping创建成功");
//            return true;
//        }else{
//            logger.info("mapping创建失败");
//            return false;
//        }
//    }
//
//    /**
//     * 创建索引,手动指定Mapping,采用XContentBuilder方式(还可以采用Json方式具体用法见上一个方法)
//     * @param index 索引名称
//     * @param mappingBuilder XContentBuilder对象构建Mapping
//     * @return
//     */
//    public static boolean createMapping(String index, String type, XContentBuilder mappingBuilder){
//
//        if (isIndexExist(index)) {
//            logger.info("Index is exits!");
//            return false;
//        }
//
//        if(mappingBuilder == null){
//            logger.info("mappingBuilder can't null");
//           return false;
//        }
//
//        CreateIndexResponse indexresponse = client.admin().indices().prepareCreate(index).execute().actionGet();
//        if(indexresponse.isAcknowledged()){
//            PutMappingRequest putMappingRequest = Requests.putMappingRequest(index).type(type).source(mappingBuilder);
//            PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();
//            if(putMappingResponse.isAcknowledged()){
//                logger.info("mapping创建成功");
//                return true;
//            }else{
//                logger.info("mapping创建失败");
//                return false;
//            }
//        }else{
//            logger.info("mapping创建失败");
//            return false;
//        }
//    }
//
//
//    /**
//     * 删除索引,会同时删除索引以及其下的所有数据/mapping/setting等
//     *
//     * @param index
//     * @return
//     */
//    public static boolean deleteIndex(String index) {
//        if (!isIndexExist(index)) {
//            logger.info("Index is not exits!");
//        }
//        DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
//        if (dResponse.isAcknowledged()) {
//            logger.info("delete index " + index + "  successfully!");
//        } else {
//            logger.info("Fail to delete index " + index);
//        }
//        return dResponse.isAcknowledged();
//    }
//
//    /**
//     * 通过ID删除数据
//     *
//     * @param index 索引，类似数据库
//     * @param type  类型，类似表
//     * @param id    数据ID
//     */
//    public static boolean deleteDataById(String index, String type, String id) {
//        DeleteResponse response = client.prepareDelete(index, type, id).execute().actionGet();
//        if (response.status().getStatus() == 200) {
//            logger.info("deleteDataById response status:{},id:{}", response.status().getStatus(),  response.getId());
//            return true;
//        }else{
//            return false;
//        }
//    }
//
//    /**
//     * 清空索引下的所有数据, 批量删除
//     * @param index
//     */
//    public static Long deleteIndexAllData(String index) {
//        SearchResponse response = client.prepareSearch(index)
//                .setQuery(QueryBuilders.matchAllQuery()).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setScroll(new TimeValue(60000)).setSize(10000).setExplain(false).execute().actionGet();
//
//        BulkRequestBuilder bulkRequest = client.prepareBulk();
//        while (true) {
//            SearchHit[] hitArray = response.getHits().getHits();
//            SearchHit hit = null;
//            for (int i = 0, len = hitArray.length; i < len; i++) {
//                hit = hitArray[i];
//                DeleteRequestBuilder request = client.prepareDelete(index, hit.getType(), hit.getId());
//                bulkRequest.add(request);
//            }
//            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
//            if (bulkResponse.hasFailures()) {
//                logger.error(bulkResponse.buildFailureMessage());
//            }
//            if (hitArray.length == 0) break;
//            response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
//        }
//
//        if (response.status().getStatus() == 200) {
//            Long counter = response.getHits().getTotalHits();
//            logger.info("bulkDelete result {} items", counter);
//            return counter;
//        } else {
//            logger.info("bulkDelete error, stauts:{}", response.status().getStatus());
//            return null;
//        }
//    }
//
//    /**
//     * 添加数据,指定ID
//     *
//     * @param jsonObject 要增加的数据
//     * @param index      索引，类似数据库
//     * @param type       类型，类似表
//     * @param id         数据ID
//     * @return
//     */
//    public static String addData(JSONObject jsonObject, String index, String type, String id) {
//        IndexResponse response = client.prepareIndex(index, type, id).setSource(jsonObject).get();
//        logger.info("addData response status:{},id:{}", response.status().getStatus(), response.getId());
//        return response.getId();
//    }
//
//    /**
//     * 数据添加,自动生成ID
//     *
//     * @param jsonObject 要增加的数据
//     * @param index      索引，类似数据库
//     * @param type       类型，类似表
//     * @return
//     */
//    public static String addData(JSONObject jsonObject, String index, String type) {
//        return addData(jsonObject, index, type, UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
//    }
//
//    //【设置自动提交文档】
//    public static BulkProcessor getBulkProcessor() {
//
//        //自动批量提交方式
//        if (staticBulkProcessor == null) {
//            try {
//                staticBulkProcessor = BulkProcessor.builder(client,
//                    new BulkProcessor.Listener() {
//                        @Override
//                        public void beforeBulk(long executionId, BulkRequest bulkRequest) {
//                            logger.info("---尝试操作" + bulkRequest.numberOfActions() + "条数据---");
//                        }
//                        @Override
//                        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
//                            // 提交结束后调用（无论成功或失败）
//                            // System.out.println(new Date().toString() + " response.hasFailures=" + response.hasFailures());
//                            logger.info( "提交" + response.getItems().length + "个文档，用时" + response.getIngestTook() + "MS" + (response.hasFailures() ? " 有文档提交失败！" : ""));
//                            // response.hasFailures();//是否有提交失败
//                            logger.info("失败原因:" + response.buildFailureMessage());
//
//                            System.out.println("---尝试操作" + request.numberOfActions() + "条数据成功---");
//                        }
//                        @Override
//                        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
//                            // 提交结束且失败时调用
//                            logger.error( " 有文档提交失败！after failure=" + failure);
//                        }
//                    })
//                    .setBulkActions(10)//文档数量达到1000时提交
//                    .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))//总文档体积达到5MB时提交 //
//                    .setFlushInterval(TimeValue.timeValueSeconds(5))//每5S提交一次（无论文档数量、体积是否达到阈值）
//                    .setConcurrentRequests(1)//加1后为可并行的提交请求数，即设为0代表只可1个请求并行，设为1为2个并行
//                    .build();
//                 //                staticBulkProcessor.awaitClose(10, TimeUnit.MINUTES);//关闭，如有未提交完成的文档则等待完成，最多等待10分钟
//            } catch (Exception e) {//关闭时抛出异常
//                e.printStackTrace();
//            }
//        }//if
//        return staticBulkProcessor;
//    }
//
//    /**
//     * 批量插入索引,ID值由调用者指定json串中哪个字段为ID
//     *
//     * @param indexQueryList
//     * @param index
//     * @param type
//     * @param idName
//     * @return
//     */
//    public static Integer bulkData(List<JSONObject> indexQueryList, String index, String type, String idName ){
//
//        int save_counter = 0;
//
//        if(indexQueryList == null || indexQueryList.size() <= 0){
//            logger.info("doc source is empty!");
//            return null;
//        }
//
//        //只有一条记录的时候调用单个保存方法
//        if(indexQueryList.size() == 1){
//            JSONObject indexObject = indexQueryList.get( 0 );
//            String id = (String)indexObject.get(idName);
//            IndexResponse response = client.prepareIndex(index, type, id).setSource(indexObject).get();
//            if(response.status().getStatus() == 200){
//                return 1;
//            }
//            return null;
//        }
//
//        //有多条记录的时候调用批量保存方法
//        if(indexQueryList.size() > 1 ) {
//
//            //数据条数记步
//            int counter = 0;
//
//            //数据上传条数设定
//            int BULK_SIZE = 500;
//
////            for (JSONObject indexJsonObject : indexQueryList){
////                String id = (String) indexJsonObject.get(idName);
////                getBulkProcessor().add(new IndexRequest(index, type,id).source(indexJsonObject));
////            }
//
//            //添加的Builder
//            BulkRequestBuilder bulkInsertRequest = client.prepareBulk();
//
//            //删除的Builder
//            BulkRequestBuilder bulkDeleteRequest = client.prepareBulk();
//            for (JSONObject indexJsonObject : indexQueryList) {
//                //索引字符串
//                //String indexSource = indexJsonObject.toJSONString();
//                //索引ID值,不同索引可能对应的idName不一样所以需要传入
//                String id = (String) indexJsonObject.get(idName);
//
//                bulkInsertRequest.add(client.prepareIndex(index, type, id).setSource(indexJsonObject));
//                bulkDeleteRequest.add(client.prepareDelete(index, type, id));
//                counter++;
//                if(counter % BULK_SIZE == 0){
//                    BulkResponse bulkResponse = bulkInsertRequest.execute().actionGet();
//                    if (bulkResponse.hasFailures()) {
//                        logger.info("有数据提交失败,执行已添加的条目回滚....");
//                        bulkDeleteRequest.execute().actionGet();
//                        logger.info("失败原因:{}", bulkResponse.buildFailureMessage());
//                    }else{
//                        save_counter = save_counter + bulkResponse.getItems().length;
//                        logger.info("索引数据成功:{}条", bulkResponse.getItems().length);
//                    }
//                    bulkInsertRequest = client.prepareBulk();
//                    bulkDeleteRequest = client.prepareBulk();
//                    counter = 0;
//                }
//            }
//
//            BulkResponse bulkResponse = bulkInsertRequest.execute().actionGet();
//            if (bulkResponse.hasFailures()) {
//                logger.info("有数据提交失败,执行已添加的条目回滚....");
//                bulkDeleteRequest.execute().actionGet();
//                logger.info("失败原因:{}", bulkResponse.buildFailureMessage());
//            }else{
//                save_counter = save_counter + bulkResponse.getItems().length;
//                logger.info("索引数据成功:{}条", bulkResponse.getItems().length);
//            }
//        }
//
//        return save_counter;
//    }
//
//    /**
//     * 通过ID 更新数据
//     *
//     * @param jsonObject 要增加的数据
//     * @param index      索引，类似数据库
//     * @param type       类型，类似表
//     * @param id         数据ID
//     * @return
//     */
//    public static void updateDataById(JSONObject jsonObject, String index, String type, String id) {
//        UpdateRequest updateRequest = new UpdateRequest();
//        updateRequest.index(index).type(type).id(id).doc(jsonObject);
//        client.update(updateRequest);
//    }
//
//    /**
//     * 通过ID获取数据
//     *
//     * @param index  索引，类似数据库
//     * @param type   类型，类似表
//     * @param id     数据ID
//     * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
//     * @return
//     */
//    public static Map<String, Object> searchDataById(String index, String type, String id, String fields) {
//        GetRequestBuilder getRequestBuilder = client.prepareGet(index, type, id);
//        if (StringUtils.isNotEmpty(fields)) {
//            getRequestBuilder.setFetchSource(fields.split(","), null);
//        }
//        GetResponse getResponse = getRequestBuilder.execute().actionGet();
//        return getResponse.getSource();
//    }
//
//    /**
//     * 使用分词查询,并分页
//     *
//     * @param index           索引名称
//     * @param startPage       当前页
//     * @param pageSize        每页显示条数
//     * @param query           查询条件
//     * @param sortField       排序字段
//     * @param highlightFields 高亮字段
//     * @return
//     */
//    public static EsPage searchDataPage(String index, QueryBuilder query, String[] highlightFields, Map<String, SortOrder> sortField, int startPage, int pageSize) {
//
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
//        searchRequestBuilder.setSearchType(SearchType.QUERY_THEN_FETCH);
//        // 设置高亮字段
//        if (highlightFields != null && highlightFields.length > 0) {
//            HighlightBuilder highlightBuilder = new HighlightBuilder();
//            highlightBuilder.preTags("<em style='color:red' >");//设置前缀
//            highlightBuilder.postTags("</em>");//设置后缀
//
//            for (int i = 0; i < highlightFields.length; i++) {
//                highlightBuilder.field(highlightFields[i]);
//            }
//            searchRequestBuilder.highlighter(highlightBuilder);
//        }
//
//        //排序字段
//        if (sortField != null && sortField.size() > 0){
//            for(Map.Entry<String, SortOrder> entry : sortField.entrySet()) {
//                searchRequestBuilder.addSort(entry.getKey(), entry.getValue());
//            }
//        }
//        //searchRequestBuilder.setQuery(QueryBuilders.matchAllQuery());
//        searchRequestBuilder.setQuery(query);
//
//        // 分页应用
//        searchRequestBuilder.setFrom(startPage).setSize(pageSize);
//
//        // 设置是否按查询匹配度排序
//        searchRequestBuilder.setExplain(true);
//
//        //打印的内容 可以在 Elasticsearch head 和 Kibana  上执行查询
//        logger.info("\n{}", searchRequestBuilder);
//
//        // 执行搜索,返回搜索响应信息
//        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
//
//        long totalHits = searchResponse.getHits().totalHits;
//        long length = searchResponse.getHits().getHits().length;
//
//        logger.debug("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);
//
//        if (searchResponse.status().getStatus() == 200) {
//            // 解析对象
//            List<Map<String, Object>> sourceList = setSearchResponse(searchResponse, highlightFields);
//            return new EsPage(startPage, pageSize, (int) totalHits, sourceList);
//        }
//        return null;
//    }
//
//    public static EsPage commonSearchDocs(String index, JSONArray queryJsonArr,int startPage,int pageSize){
//
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.from(startPage);
//        sourceBuilder.size(pageSize);
//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        if(queryJsonArr != null && queryJsonArr.size() > 0){
//            for(int i = 0; i< queryJsonArr.size(); i++){
//                JSONObject jsonObject = (JSONObject)queryJsonArr.get(i);
//                String sfield  = (String)jsonObject.get("sfield");
//                String svalue  = (String)jsonObject.get("svalue");
//                String stype   = (String)jsonObject.get("stype");
//                String contype = (String)jsonObject.get("contype");
//                if("and".equals(contype)){        //and连接
//                    if("equals".equals(stype)){      //等值查询
//                        boolQueryBuilder.must(QueryBuilders.termQuery(sfield, svalue));
//                    }else if("like".equals(stype)){  //匹配查询
//                        boolQueryBuilder.must(QueryBuilders.matchQuery(sfield, svalue));
//                    }
//                }else if("or".equals(contype)){   //or连接
//                    if("equals".equals(stype)){       //等值查询
//                        boolQueryBuilder.should(QueryBuilders.termQuery(sfield, svalue));
//                    }else if("like".equals(stype)){   //匹配查询
//                        boolQueryBuilder.should(QueryBuilders.matchQuery(sfield, svalue));
//                    }
//                }
//            }
//        }
//
//        sourceBuilder.query(boolQueryBuilder);
//
//        // 执行搜索,返回搜索响应信息
//        SearchResponse searchResponse = searchRequestBuilder.setSource(sourceBuilder).execute().actionGet();
//        long totalHits = searchResponse.getHits().totalHits;
//        long length = searchResponse.getHits().getHits().length;
//        logger.debug("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);
//
//        if (searchResponse.status().getStatus() == 200) {
//            //解析对象,返回数据
//            List<Map<String, Object>> sourceList = setSearchResponse(searchResponse, null);
//            return new EsPage(startPage, pageSize, (int) totalHits, sourceList);
//        }
//
//        return null;
//    }
//
//    /**
//     * 使用分词查询
//     *
//     * @param index           索引名称
//     * @param type            类型名称,可传入多个type逗号分隔
//     * @param query           查询条件
//     * @param size            文档大小限制
//     * @param fields          需要显示的字段，逗号分隔（缺省为全部字段）
//     * @param sortField       排序字段
//     * @param highlightFields 高亮字段
//     * @return
//     */
//    public static List<Map<String, Object>> searchListData(String index, String type, QueryBuilder query, Integer size, String fields, String sortField, String[] highlightFields) {
//
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
//        if (StringUtils.isNotEmpty(type)) {
//            searchRequestBuilder.setTypes(type.split(","));
//        }
//
//        // 设置高亮字段
//        HighlightBuilder highlightBuilder = new HighlightBuilder();
//        highlightBuilder.preTags("<em>");//设置前缀
//        highlightBuilder.postTags("</em>");//设置后缀
//        if(highlightFields !=null && highlightFields.length > 0){
//            for(int i= 0; i< highlightFields.length; i++){
//                highlightBuilder.field(highlightFields[i]);
//            };
//        }
//
//        searchRequestBuilder.highlighter(highlightBuilder);
//        searchRequestBuilder.setQuery(query);
//
//        if (StringUtils.isNotEmpty(fields)) {
//            searchRequestBuilder.setFetchSource(fields.split(","), null);
//        }
//        searchRequestBuilder.setFetchSource(true);
//
//        if (StringUtils.isNotEmpty(sortField)) {
//            searchRequestBuilder.addSort(sortField, SortOrder.DESC);
//        }
//
//        if (size != null && size > 0) {
//            searchRequestBuilder.setSize(size);
//        }
//
//        //打印的内容 可以在 Elasticsearch head 和 Kibana  上执行查询
//        logger.info("\n{}", searchRequestBuilder);
//
//        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
//
//        long totalHits = searchResponse.getHits().totalHits;
//        long length = searchResponse.getHits().getHits().length;
//
//        logger.info("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);
//
//        if (searchResponse.status().getStatus() == 200) {
//            // 解析对象
//            return setSearchResponse(searchResponse, highlightFields);
//        }
//        return null;
//
//    }
//
//
//    /**
//     * 高亮结果集 特殊处理
//     *
//     * @param searchResponse
//     * @param highlightFields
//     * @return
//     */
//    private static List<Map<String, Object>> setSearchResponse(SearchResponse searchResponse, String[] highlightFields) {
//        List<Map<String, Object>> sourceList = new ArrayList<Map<String, Object>>();
//        for (SearchHit searchHit : searchResponse.getHits().getHits()) {
//            StringBuffer stringBuffer = new StringBuffer();
//            searchHit.getSourceAsMap().put("id", searchHit.getId());
//            if ( highlightFields!=null && highlightFields.length > 0 ) {
//                logger.info("遍历 高亮结果集, 覆盖正常结果集" + searchHit.getSourceAsMap());
//                for(String field : highlightFields) {
//                    Map<String, HighlightField> matchHighlightFields = searchHit.getHighlightFields();
//                    if(matchHighlightFields!=null){
//                        HighlightField matchHighLightField = matchHighlightFields.get(field);
//                        if(matchHighLightField!=null){
//                            Text[] text = matchHighLightField.getFragments();
//                            if (text != null) {
//                                for (Text str : text) {
//                                    stringBuffer.append(str.string());
//                                }
//                                //遍历 高亮结果集，覆盖 正常结果集
//                                searchHit.getSourceAsMap().put(field, stringBuffer.toString());
//                            }
//                        }
//                    }
//                }
//            }
//            sourceList.add(searchHit.getSourceAsMap());
//        }
//        return sourceList;
//    }
//
//
//    public static EsPage searchNearAndTermQuery(String index, String geoField, Double lat, Double lon, Long distance, Map<String, String> termMaps, int startPage, int pageSize) {
//
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.from(startPage);
//        sourceBuilder.size(pageSize);
//        sourceBuilder.timeout(new TimeValue(distance, TimeUnit.SECONDS));
//
//        //Geo查询器
//        QueryBuilder geoQuery = new GeoDistanceQueryBuilder(geoField)
//                .point(lat, lon)
//                .distance(distance, DistanceUnit.METERS)       // 指定位置为中心的圆的半径,单位:米
//                //.distance(distance, DistanceUnit.KILOMETERS) // 指定位置为中心的圆的半径,单位:千米
//                .geoDistance(GeoDistance.PLANE);               // 按平面计算距离，平面(更快，但在长距离和靠近极点的地方是不准确的)而立方(default)
//
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        boolQueryBuilder.must(geoQuery);
//
//        //额外条件查询
//        for(Map.Entry<String, String> entry : termMaps.entrySet()) {
//            boolQueryBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
//        }
//
//        sourceBuilder.query(boolQueryBuilder);
//        //Geo排序方式
//        GeoDistanceSortBuilder geoSort = SortBuilders.geoDistanceSort(geoField, lat, lon)
//                .order(SortOrder.ASC) // 最近的排在最前面
//                .unit(DistanceUnit.KILOMETERS);
//
//        // 执行搜索,返回搜索响应信息
//        SearchResponse searchResponse = searchRequestBuilder.setSource(sourceBuilder).addSort(geoSort).execute().actionGet();
//
//        long totalHits = searchResponse.getHits().totalHits;
//        long length = searchResponse.getHits().getHits().length;
//        logger.debug("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);
//        if (searchResponse.status().getStatus() == 200) {
//            SearchHits hits = searchResponse.getHits();
//            SearchHit[] searchHits = hits.getHits();
//
////            logger.info("您当前位置为：[" + lon + "," + lat + "]，开始搜索附近 " + distance + "KM 以内的朋友...");
////            logger.info("检索完成!总耗时:" + searchResponse.getTook().getMillis() + "毫秒,符合条件的有 " + searchHits.length + " 个!");
//            for (SearchHit hit : searchHits) {
////                String sourceAsString = hit.getSourceAsString();
//                BigDecimal geoDistance = new BigDecimal((double) hit.getSortValues()[0]).setScale(6, BigDecimal.ROUND_HALF_DOWN);//四舍五入
////                System.out.println(sourceAsMap.get("organization_name") + " 距您 " + geoDistance + "M,source:" + sourceAsString);
//                //讲计算得到的距离写入结果集中
//                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//                hit.getSourceAsMap().put("distance",geoDistance);
//            }
//
//            //解析对象,返回数据
//            List<Map<String, Object>> sourceList = setSearchResponse(searchResponse, null);
//            return new EsPage(startPage, pageSize, (int) totalHits, sourceList);
//        }
//        return null;
//    }
//
//
//    /**
//     * 搜索附近
//     *
//     * @param index     索引
//     * @param fieldName 经纬度字段名称
//     * @param lat       纬度
//     * @param lon       精度
//     * @param distance  范围值
//     * @param startPage 开始页数
//     * @param pageSize  每页条数
//     * @return
//     */
//    public static EsPage searchNear(String index, String fieldName, Double lat, Double lon, Long distance, int startPage, int pageSize) {
//
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.from(startPage);
//        sourceBuilder.size(pageSize);
//        sourceBuilder.timeout(new TimeValue(distance, TimeUnit.SECONDS));
//
//        //Geo查询器
//        QueryBuilder geoQuery = new GeoDistanceQueryBuilder(fieldName)
//                .point(lat, lon)
//                .distance(distance, DistanceUnit.METERS)       // 指定位置为中心的圆的半径,单位:米
//                //.distance(distance, DistanceUnit.KILOMETERS) // 指定位置为中心的圆的半径,单位:千米
//                .geoDistance(GeoDistance.PLANE);               // 按平面计算距离，平面(更快，但在长距离和靠近极点的地方是不准确的)而立方(default)
//        sourceBuilder.query(geoQuery);
//
//        //Geo排序方式
//        GeoDistanceSortBuilder geoSort = SortBuilders.geoDistanceSort(fieldName, lat, lon)
//                .order(SortOrder.ASC) // 最近的排在最前面
//                .unit(DistanceUnit.KILOMETERS);
//        sourceBuilder.sort(geoSort);
//
//        // 执行搜索,返回搜索响应信息
//        SearchResponse searchResponse = searchRequestBuilder.setSource(sourceBuilder).execute().actionGet();
//
//        long totalHits = searchResponse.getHits().totalHits;
//        long length = searchResponse.getHits().getHits().length;
//        logger.debug("共查询到[{}]条数据,处理数据条数[{}]", totalHits, length);
//        if (searchResponse.status().getStatus() == 200) {
//            SearchHits hits = searchResponse.getHits();
//            SearchHit[] searchHits = hits.getHits();
//
////            logger.info("您当前位置为：[" + lon + "," + lat + "]，开始搜索附近 " + distance + "KM 以内的朋友...");
////            logger.info("检索完成!总耗时:" + searchResponse.getTook().getMillis() + "毫秒,符合条件的有 " + searchHits.length + " 个!");
//            for (SearchHit hit : searchHits) {
////                String sourceAsString = hit.getSourceAsString();
//                BigDecimal geoDistance = new BigDecimal((double) hit.getSortValues()[0]).setScale(6, BigDecimal.ROUND_HALF_DOWN);//四舍五入
////                System.out.println(sourceAsMap.get("organization_name") + " 距您 " + geoDistance + "M,source:" + sourceAsString);
//                //讲计算得到的距离写入结果集中
//                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//                hit.getSourceAsMap().put("distance",geoDistance);
//            }
//
//            //解析对象,返回数据
//            List<Map<String, Object>> sourceList = setSearchResponse(searchResponse, null);
//            return new EsPage(startPage, pageSize, (int) totalHits, sourceList);
//        }
//        return null;
//    }
//
//    /**
//     * 建议搜索/自动补全搜索
//     * @param index        索引
//     * @param keyword      搜索词
//     * @param field        自动补全字段
//     * @param suggestField 自动补全字段
//     * @param pageSize     自动补全多少条
//     * @return
//     */
//    public static List<String> searchSuggest(String index, String keyword, String field, String suggestField, Integer pageSize) {
//        List<String> result = new ArrayList<>();
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
//        //completion suggest
//        CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder(field);
//        //前缀查询 每次返回最多10条数据
//        completionSuggestionBuilder.prefix(keyword).size(pageSize);
//
//        //suggestField : 自定义的类型为complete的字段名
//        SuggestBuilder suggestBuilder = new SuggestBuilder().addSuggestion(suggestField, completionSuggestionBuilder);
//        SearchResponse searchResponse = searchRequestBuilder.suggest(suggestBuilder).execute().actionGet();
//
//        //保存es返回结果
//        List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> list = searchResponse
//                .getSuggest().getSuggestion(suggestField).getEntries();
//        if (list == null) {
//            return null;
//        }
//        else {
//            //转为list保存结果字符串
//            for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> e : list) {
//                for (Suggest.Suggestion.Entry.Option option : e) {
//
//                    result.add(option.getText().toString());
//
//                    logger.info(option.getText().toString());
//                }
//            }
//        }
//        return result;
//    }
//
//}