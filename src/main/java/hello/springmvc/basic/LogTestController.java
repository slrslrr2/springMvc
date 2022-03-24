package hello.springmvc.basic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 method에 String을 리턴하면
    @Controller을 선언한 경우 : 뷰를 찾고 뷰가 렌더링된다.
    @RestController       : 현 화면의 httpBody에 메시지가 바디에 바로 입력된다.
                            RestAPI만들 때 자주 사용하는 기능이다.
 */
@Slf4j
@RestController
public class LogTestController {
//    private final Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping("/log-test")
    public String logTest(){
        String name = "Spring";

        System.out.println("name = " + name);


        log.trace("info log = {}"+name); //이렇게하면 사용은 가능하지만
                                        // 만약 level을 info로 한 경우
                                        // trace는 실행안하지만 +연산을 자바가 하기때문에
                                        // 필요없는 연산을 하기에 메모리 낭비가 된다.
        log.info("info log = {}", name);
        return "ok";
    }
/**
 name = Spring
 2022-03-24 20:36:37.834  INFO 4549 --- [nio-8080-exec-2] hello.springmvc.basic.LogTestController  : info log = Spring
    4549는 process id이다.
    nio-8080-exec-2 : 클라이언트 요청 시 쓰레드가 생성된다. 생성된 [쓰레드 이름]이다.
    hello.springmvc.basic.LogTestController 호출된 컨트롤러 이름
    info log = Spring 우리가 찍은 내용이다!

    해당 로그의 정보는 아래 파일에서 다음과 같이 남기면 log의 레벨을 설정할 수 있다.
    파일명 : application.properties
    logging.level.hello.springmvc=trace
 */

    @RequestMapping("/log-test2")
    public String logTest2(){
        String name = "String";

        // log를 찍을 때 레벨을 정할 수 있다.
        log.trace("trace log = {}", name);
        log.debug("debug log = {}", name);
        log.info("warn log = {}", name); // 개발서버
        log.warn("warn log = {}", name); // 경고
        log.error("error log = {}", name); // 에러

        return "ok";
    }
}
