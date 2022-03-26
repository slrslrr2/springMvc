package hello.springmvc.basic.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.springmvc.basic.HelloData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
public class RequestBodyJsonController {

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * {"username":"hello", "age":20}
     * content-type: application/json
     */
    @PostMapping("/request-body-json")
    public void requestbodyJsonV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);

        log.info("helloData => " + helloData);

        response.getWriter().write("ok");
    }

    @ResponseBody
    @PostMapping("/request-body-json2")
    public String requestbodyJsonV2(@RequestBody String messageBody) throws IOException {
        HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
        log.info("helloData => " + helloData);

        return "ok2";
    }

    /**
     *
     * 앞서 배운 내용과 같이 @ModelAttribute, @RequestParam은 생략 가능하다
     * 단, GET이나 POST방식으로 가져온 경우 데이터를 바인딩 할 수 있다.
     *
     * 만약 application/json으로 데이터를 가지고 온 경우
     * @RequestBody를 사용하게 되는데
     *
     * (HelloData helloData) 파라미터를 받을 경우 이렇게 사용하였다면 hellData는 null로 들어갈 것이다.
     * Why? 생략하면 객체로 받는거니까 @ModelAttribute가 적용되기 때문이다.
     */
    @ResponseBody
    @PostMapping("/request-body-json3")
    public String requestbodyJsonV3(@RequestBody HelloData helloData) {
        log.info(helloData.toString());
        return "ok3";
    }

    @ResponseBody
    @PostMapping("/request-body-json4")
    public String requestbodyJsonV4(HttpEntity<HelloData> httpEntity) {
        HelloData data = httpEntity.getBody();
        log.info(data.toString());
        return "ok4";
    }


    /**
     * @ResponseBody를 사용하면
     * return 을 객체로 할 수 있다.
     *
     * 요청 : {"username":"hello", "age":20}
     * 응답 : {"username":"hello", "age":20}
     */
    @ResponseBody
    @PostMapping("/request-body-json5")
    public HelloData requestbodyJsonV5(@RequestBody HelloData helloData) {
        log.info(helloData.toString());
        return helloData;
    }

}