package com.sl.community.service.impl;

import com.sl.community.entity.DiscussPost;
import com.sl.community.service.ElasticsearchService;
import com.sl.community.dao.elasticsearch.DiscussPostRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    //向es服务器里提交新增帖子
    public void saveDiscussPost(DiscussPost post){
        discussRepository.save(post);
    }
    //向es服务器里提交删除帖子
    public void deleteDiscussPost(int id){
        discussRepository.deleteById(id);
    }

    //搜索方法(传入关键字，当前第几页，每页多少条数据)
    public Page<DiscussPost> searchDiscussPost(String keyword,int current,int limit){
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))//搜索条件（即从title和content）
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))//排序条件（按照type是否置顶，score帖子分值，时间依次排序）
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current,limit))//分页条件
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();//哪些词高亮显示（前后置标签）

        return elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits=response.getHits();//获取命中的数据
                if(hits.getTotalHits()<=0){
                    return null;
                }

                //遍历命中的数据
                List<DiscussPost> list=new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost post=new DiscussPost();
                    //将数据包装到实体类返回（在hit里将json封装成了map）
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));//将字符串id转为数字

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));
                    //获得原始的title，content（可能里面没有关键词在content里）
                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    //日期是long类型的字符串，将其先转为long再转为日期
                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    //处理高亮显示的结果
                    //（获得高亮显示的内容）
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if(titleField!=null){
                        //不为空，重新设置title（只要第一个高亮部分）
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if(contentField!=null){
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);

                }

                return new AggregatedPageImpl(list,pageable,
                        hits.getTotalHits(),response.getScrollId(),hits.getMaxScore());
            }
        });

    }


}
