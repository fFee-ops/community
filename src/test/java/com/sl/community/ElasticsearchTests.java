package com.sl.community;

import com.sl.community.entity.DiscussPost;
import com.sl.community.dao.DiscussPostMapper;
import com.sl.community.dao.elasticsearch.DiscussPostRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;

    @Test
    public void testInsert(){
        discussRepository.save(discussMapper.selectDiscussById(241));
        discussRepository.save(discussMapper.selectDiscussById(242));
        discussRepository.save(discussMapper.selectDiscussById(243));
    }

    @Test
    public void testInsertList(){
        discussRepository.saveAll(discussMapper.selectDiscussPosts(101,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(102,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(111,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(112,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(131,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(132,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(133,0,100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(134,0,100,0));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussMapper.selectDiscussById(231);
        post.setContent("我是新人，摸鱼摸鱼");
        discussRepository.save(post);
    }
    //搜索的关键词，排序方式，分页方式，高亮显示
    @Test
    public void testSearchByRepository(){
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))//搜索条件（即从title和content）
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))//排序条件（按照type是否置顶，score帖子分值，时间依次排序）
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))//分页条件
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();//哪些词高亮显示（前后置标签）

        //多少行，多少页，当前在哪一页，当前页显示了多少条数据
        Page<DiscussPost> page = discussRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    @Test
    public void testSearchTemplate(){
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))//搜索条件（即从title和content）
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))//排序条件（按照type是否置顶，score帖子分值，时间依次排序）
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))//分页条件
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();//哪些词高亮显示（前后置标签）

        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits=response.getHits();
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

        //多少行，多少页，当前在哪一页，当前页显示了多少条数据
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    @Test
    public void testDelete(){
        discussRepository.deleteById(231);
    }


}
