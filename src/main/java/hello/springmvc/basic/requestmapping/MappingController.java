package hello.springmvc.basic.requestmapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class MappingController {

    @GetMapping("hello-basic")
    public String helloBasic(){
        log.info("helloBasic");
        return "ok";
    }

    /**
     * @PathVariable : 경로 변수를 설정하여 받을 수 있다
     *
     * 아래는 같은 의미이다. 변수명과 가져올 이름이 같다면 생략 가능하다.
     * @PathVariable String userId
     * @PathVariable("userId") String userId
     *
     * 요즘은 Path리소스에 식별자를 넣는 스타일을 선호한다.
     */
    @GetMapping("/mapping/{userId}")
    public String mappingPath(@PathVariable String userId){
        log.info("userId => {}", userId);
        return "OK";
    }
}
