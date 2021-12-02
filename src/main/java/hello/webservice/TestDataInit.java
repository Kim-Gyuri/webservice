package hello.webservice;

import hello.webservice.domain.posts.Posts;
import hello.webservice.domain.posts.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final PostsRepository postsRepository;

    /**
     * 테스트용 데이터 추가
     */
    @PostConstruct
    public void init() {
        postsRepository.save(new Posts("테스트1", "test1@gmail.com", "테스트1의 본문"));

    }

}