## 메시지 컨버터의 사용과 RequestMappingHandlerAdapter 동작방식



클라이언트에서 서버로 요청데이터를 전달하는 방법은 크게 3가지가 있다.

1. GET - **쿼리 파라미터**
   - /url**?username=hello&age=20**
   - **메시지 바디 없이**(Content-type 가 없다), URL의 쿼리 파라미터에 데이터를 포함해서 전달
   - 예) 검색, 필터, 페이징등에서 많이 사용하는 방식
2. POST HTML Form
   - content-type: application/x-www-form-urlencoded
   - **메시지 바디에 쿼리 파리미터** 형식으로 전달 username=hello&age=20
   - 예) 회원 가입, 상품 주문, HTML Form 사용
3. Request Body에 데이터를 직접 담아서 요청 
   - HTTP API에서 주로 사용, JSON, XML, TEXT
   - 데이터 형식은 주로 **JSON** 사용
   - POST, PUT, PATCH



이 중에서 3번 RequestBody에 데이터를 직접 담아서 요청하는방법은 <br>Controller에서 **@RequestBody**를 통해 **String이나 HelloData와 같은 객체**로 직접 받을 수 있다.

##### 요청 데이터 방식

```java
{"username":"hello", "age":20}
content-type: application/json
```

~~~java
@ResponseBody
@PostMapping("/request-body-json-v2")
public String requestBodyJsonV2(@RequestBody String messageBody) throws IOException {
  HelloData data = objectMapper.readValue(messageBody, HelloData.class);
  return "ok";
}

@ResponseBody
@PostMapping("/request-body-json-v3")
public HelloData requestBodyJsonV3(@RequestBody HelloData data) {
  log.info("username={}, age={}", data.getUsername(), data.getAge());
  return HelloData;
}

~~~



그렇다면 @RequestBody String, @RequestBody Entity등을 유연하게 Argument로 받을 수 있는걸까?<br> handlerAdapter안에 **HTTP 메시지 컨버터**를 사용하기 때문이다.

![image-20220328202239658](image-20220328202239658.png)



## 메시지 컨버터는 어디쯤 있을까?

조건 : 

1. JSON으로 던진다.
2. Controller의 Url이 **@RequeestMapping**
3. **@RequestBody String**으로 데이터를 받는다.

```java
ontent-type: application/json
{"username":"hello", "age":20}
```

```java
@RequestMapping("/request-body-json-v2")
public void requestBodyJsonV2(@RequestBody String messageBody) throws IOException {
  HelloData data = objectMapper.readValue(messageBody, HelloData.class);
  return "ok";
}
```



![image-20220328203728863](image-20220328203728863.png)

1. DispatcherServlet은 조건에 맞는 handler를 찾아 반환한 후 handlerAdapter를 찾는다.

   ##### DispatcherServlet.java

   ```java
   protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
   
     // Determine handler for the current request.
     mappedHandler = getHandler(processedRequest);
   
     // Determine handler adapter for the current request.
     // handlerAdapter는 @RequestMapping으로 URL을 등록하였다는 전제하이기에
     // RequestMappingHandlerAdapter.java가 받아와진다.
     HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
   
     // Actually invoke the handler.
     // 
     mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
   ```

   > **HandlerAdapter 의 ha.handle**메소드 안에는<br> **ArgumentResolver**, **HTTP 메시지 컨버터**를 통해 **Controller의 Argument를 매핑**해주는 내용이 있다.

   

   > HandlerAdapter ha.handle를 interface로 선언함으로써 <br>SOLID원칙 - **OCP: 개방-폐쇄 원칙(Open/closed principle)**
   >
   > - 소프르웨어 요소는 **확장에는 열려** 있으나 **변경애는 닫혀** 있어야 한다. ==> 다형성
   > - interface로 작성하여 구현체를 유연하게 받을 수 있도록 설계된것이다.
   >
   >  @RequestMapping의 경우 HandlerAdaper를 상속받은 **RequestMappingHandlerAdaper**를 사용한다.
   >
   > > HandlerAdapter
   > > **0 = RequestMappingHandlerAdapter**
   > >    : 애노테이션 기반의 컨트롤러인
   > >      @RequestMapping에서 사용
   > >
   > > **1 = HttpRequestHandlerAdapter**
   > >   : HttpRequestHandler 처리
   > >
   > > **2 = SimpleControllerHandlerAdapter**
   > >    : Controller 인터페이스
   > >      (애노테이션X, 과거에 사용) 처리

   

   ![image-20220328211049852](image-20220328211049852.png)

   

   > 그렇다면 RequestMappingHandlerAdapter의 handle메소드에서 <br>**ArgumentResolver**, **HTTP 메시지 컨버터**를 통해 **Controller의 Argument를 매핑**해주는 내용을 살펴보자

   

   ##### AbstractHandlerMethodAdapter.java

   ```java
   @Override
   @Nullable
   public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
     throws Exception {
     return handleInternal(request, response, (HandlerMethod) handler);
   }
   
   @Nullable
   protected abstract ModelAndView handleInternal(HttpServletRequest request,
                                                  HttpServletResponse response, HandlerMethod handlerMethod) throws Exception;
   ```

   > handle의 로직을 실행하는 handleInternal 메소드는 abstract로 이루워졌다.<br>이 안에 **ArgumentResolver**, **HTTP 메시지 컨버터**를 통해 **Controller의 Argument를 매핑**해주는 내용을 살펴보자



##### RequestMappingHandlerAdapter.java

```java
@Override
	protected ModelAndView handleInternal(HttpServletRequest request,
		HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		// Execute invokeHandlerMethod in synchronized block if required.
    // invoke : 호출
		mav = invokeHandlerMethod(request, response, handlerMethod);

		return mav;
	}
```

> 해당 메소드 안에는 **invokeHandlerMethod** 를 무조건 실행하는데 한번 들어가보즈아

```java
@Nullable
protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                           HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		// 여기에서 사용되는 argumentResolvers는 
    // HandlerMethodArgumentResolverComposite implements HandlerMethodArgumentResolver
    if (this.argumentResolvers != null) {
      invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
    }
    if (this.returnValueHandlers != null) {
      invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
    }
```

> **invokeHandlerMethod** 안에는 **this.argumentResolvers**이 선언되어있다.
>
> 해당 변수가 무엇일까?<br>ArgumentResolver 를 호출해서 컨트롤러(핸들러)가 필요로 하는 다양한 파라미터의 값(객체)을 생성한다. 그리고 이렇게 파리미터의 값이 모두 준비되면 컨트롤러를 호출하면서 값을 넘겨준다.





ㅠㅠ



begin tran

select ~~ 



-- commit tran

JsonPathArgumentResolver

![image-20220327122652045](image-20220327122652045.png)



![image-20220329205412451](image-20220329205412451.png)



-------------

메시지 컨버터는 어떻게 동작할까?

**HttpMessageConverter** 

```java
public interface HttpMessageConverter<T> {
  boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);
  boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);

  List<MediaType> getSupportedMediaTypes();
  
  T read(Class<? extends T> class, HttpInputMessage inputMessage) 
    throws IOException, HttpMessageNotReadableException;
  
  void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
    throws IOException, HttpMessageNotWritableException;
}
```



만약 **content-type: application/json** 의 데이터가 **void hello(@RequestBody String data) {}** 라는 Controller 메소드에 들어왔다면<br>클라이언트의 content-type을 String으로 받는다고 선언하였기 때문에 아래 HttpMessageConverter를 상속받은 객체들이 루프를 돌면서 String 컨버터를 확인한다.

```
																				미디어타입
0 = ByteArrayHttpMessageConverter				*/*
1 = StringHttpMessageConverter					*/* 									! 음 적절하군
2 = MappingJackson2HttpMessageConverter	application/json관련
```



그렇게 되면 **StringHttpMessageConverter**가 1차로 선택이 되는데, 해당 컨버터가 **content-type: application/json** 를 받아들일 수 있는지 미디어 타입을 확인한다.<br>StringHttpMessageConverter는 미디어타입을 \*/\*로 받아들일 수 있기에 선택된다!

만약 아래 로 한다면 

```
content-type: text/html
@RequestMapping
void hello(@RequetsBody HelloData data) {}
```

클래스(HelloData)타입이니까 **MappingJackson2HttpMessageConverter**을 선택하겠지만, 미디어타입이 text/html로 요청하여 받아들일 수 없기에 이는 415 error가 발생한다

![image-20220327111543413](image-20220327111543413.png)

