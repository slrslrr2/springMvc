package hello.springmvc.basic.requestmapping;

import hello.springmvc.basic.HelloData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
@Slf4j
public class RequestParamController {

    @RequestMapping("/request-param-v1")
    public void requestParamV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        log.info("username => {}, age => {}", username, age);

        response.getWriter().println("ok");
    }

    /**
     * response.getWriter().println("ok"); 해서 응답을 주는것이 아닌
     *      @RequestParam
     *      숫자도 자동으로 받아준다(request.getParameter는
     * @RestController방식도 있지만 @ResponseBody를 사용해서
     *      직접 HTTP message Body에 직접 해당 내용 입력
     */
    @ResponseBody
    @RequestMapping("/request-param-v2")
    public String requestParamV2(@RequestParam("username") String username,
                                 @RequestParam("age") int age) throws IOException {
        log.info("username => {}, age => {}", username, age);
        return "ok";
    }

    /**
     * @RequestParam에서 받은변수명과 Controller에서 사용할 변수명이 같으면 생략가능
     *
     * ex)
     * @RequestParam("username") String username
     * @RequestParam String username
     */
    @ResponseBody
    @RequestMapping("/request-param-v3")
    public String requestParamV3(@RequestParam String username,
                                 @RequestParam int age) throws IOException {
        log.info("username => {}, age => {}", username, age);
        return "ok";
    }

    /**
    @RequestParam 생략 가능
     */
    @ResponseBody
    @RequestMapping("/request-param-v4")
    public String requestParamV4(String username, int age) {
        log.info("username => {}, age => {}", username, age);
        return "ok";
    }

    /**
     * 필수값 지정
     @RequestParam(required = true) default
     @RequestParam(required = false)

     파라미터를 /request-param-v5?username=
     으로 넘기면 빈값으로 들어온다.
     */
    @ResponseBody
    @RequestMapping("/request-param-v5")
    public String requestParamV5(@RequestParam(required = true) String username,
                                 @RequestParam(required = false) int age) throws IOException {
        log.info("username => {}, age => {}", username, age);
        return "ok";
    }

    /**
     * 필수값 지정
     @RequestParam(required = true) default
     @RequestParam(required = false)

     파라미터를 /request-param-v5?username=
     으로 넘겨도 DefaultValue를 설정하면 DefaultValue로 셋팅된다
     */
    @ResponseBody
    @RequestMapping("/request-param-default")
    public String requestParamDefalult(@RequestParam(required = true) String username,
                                       @RequestParam(required = false, defaultValue = "1") int age) throws IOException {
        log.info("username => {}, age => {}", username, age);
        return "ok";
    }

    // paramMap = {usernmae=gbitkim, age=30}
    @ResponseBody
    @RequestMapping("/request-param-map")
    public String requestParamMap(@RequestParam Map<String, Object> paramMap) throws IOException {
        System.out.println("paramMap = " + paramMap);
        return "ok";
    }

    // paramMap = {usernmae=[gbitkim], age=[30]}
    @ResponseBody
    @RequestMapping("/request-param-multi-value-map")
    public String requestParamMultiValueMap(@RequestParam MultiValueMap<String, Object> paramMap) throws IOException {
        System.out.println("paramMap = " + paramMap);
        return "ok";
    }

    /**
     객체릴 바인딩 하기 위한 @ModelAttribute는 GET이나 POST방식으로만 사용가능하다.

     String username, @RequestParam int age

     HelloData helloData = new HelloData();
     helloData.setUsername(username);
     helloData.setAge(age);

     @ModelAttribute를 사용하면 HelloData 객체에 자동으로 주입된다.
        @ModelAttribute을 사용하면 setter을 호출해서 파라미터 값을 바인딩한다.

        HelloData 객체 안에 @Data를 선언함으호써 @Setter, @Getter, @ToString, @RequiredArgsConstructor, @EqualsAndHashCode를 자동으로 생성해준다
        이때, @ToString이 있으므로
        객체를 log에 찍으면 데이터가 아래처럼 얘쁘게 나온다
        HelloData => HelloData(username=gbitkim, age=20)


     */
    @ResponseBody
    @RequestMapping("/model-attribute-v1")
    public String modelAttributeV1(@ModelAttribute HelloData helloData){
        log.info("HelloData => "+ helloData);
        return "ok";
    }

    // ModelAttribute는 생략가능하다
    @ResponseBody
    @RequestMapping("/model-attribute-v2")
    public String modelAttributeV2(@ModelAttribute HelloData helloData){
        log.info("HelloData => "+ helloData);
        return "ok";
    }
}
