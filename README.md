### ArgumentResolver, MessageConverter의 동작 흐름

#### Tomcat시작부터 -> DispatcherServlet -> ArgumentResolver -> MessageConvertor 큰 흐름입니다.

```java
첫번째 Tomcat 작동 시 
    - RequestMappingHandlerAdapter을 생성
      - messageConverters
      - argumentResolvers
      - returnValueHandlers들을 셋팅한 뒤, 스프링 컨테이너에 등록
    - RequestMappingHandlerMapping 생성 및 스프링 컨테이너에 등록

두번째 Client의 첫 요청 시 DispatcherServlet객체를 생성한 후 요청을 처리한다.
    - 이는 싱글톤으로 관리되기에 
       - 첫 요청시에만 생성
    - 이 때, Servlet안에 스프링컨테이너를 탐색하여 handlerAdaptro와 mapping을 변수에 등록

세번째 doDispatch 함수를 통해 스프링 요청 프로세스 진행
    - 여기서부터는 두번째 요청, 세번째 요칭등이 들어오는 실행 로직과 동일
 
네번째 doDispatch안의 흐름을 타서 ArgumentResolver, MessageConvertor을 통해 Controller가 원하는 Object 반환

ArgumentResolver [RequestResponseBodyMethodProcessor]
  1. 해당 파라미터를 지원해주는 ArgumentResolver가 있는지 확인  
  boolean supportsParameter(MethodParameter parameter);
	
  2. [1]에서 확인한 ArgumentResolver를 실행하여
      ㄴ 해당 Controller에서 원하는 Argument를 Return
          ㄴ 이 안에 messageConverter 을 사용해서 @RequestBody 파라미터를 받을 수 있게 도와줌
  Object resolveArgument( ... 생략 ...) throws Exception;

 3. ArgumentResolver안의 readWithMessageConverters메소드에서
      for문을 돌려 알맞은 MessageConvertor를 확인하고
          ㄴ MessageConvertor안의
		1. canRead 메소드로 MessageConvertor로 @RequestBody를 읽을 수 있는지 확인
		2. read 함수를 통해 Controller에서 원하는 객체 반환

다섯번째, Controller에 해당 Arguement 리턴 해준다
```


-------
<br>

#### 아래 설명할 전제조건

> @RequestMapping을 통해 요청
>
> handlerAdaptor : RequestMappingHandlerAdaptor<br>handlerMapping : RequestMappingHandlerMapping<br>

```java
// 요청데이터
content-type: application/json
{"username":"hello", "age":20}

// Controller
// 응답 @RequestBody를 통해 String이나 HelloData와 같은 객체로 직접 받을 수 있다.
@ResponseBody
@RequestMapping("test/request1")
public Hello test(@RequestBody Hello hello){
  return hello;
}
```

-----------


<br>


처음 궁금했던 내용이 DispatcherServlet은 클라이언트 요청이 들어오면 HandlerMapping, HandlerAdapter <br>for문을 돌려서 알맞은 adaptor와 mapping을 사용하는데 <br>그렇다면 handler와 mapping의 데이터들을 언제 셋팅해주는지가 궁금
<img width="711" alt="image-20220411205927236" src="https://user-images.githubusercontent.com/58017318/162986284-4f916255-2b6d-4eda-93b2-f8c2691c1fd1.png">


<br><br><br>

## 0. Tomcat이 처음 작동 시 

### 가. **RequestMappingHandlerAdapter**을 생성하고 <br>messageConverters, argumentResolvers, returnValueHandlers들을 셋팅한 뒤, 스프링빈에 등록한다.

WebMvcConfigurationSupport.java

```java
@Bean
public RequestMappingHandlerAdapter requestMappingHandlerAdapter( ...생략 ... ) {
  RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
  
  // RequestMappingHandlerAdapter에 변수들을 셋팅해준다.
  adapter.setContentNegotiationManager(contentNegotiationManager);
  adapter.setMessageConverters(getMessageConverters());
  adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer(conversionService, validator));
  adapter.setCustomArgumentResolvers(getArgumentResolvers());
  adapter.setCustomReturnValueHandlers(getReturnValueHandlers());
```



1) **messageConverters**

- Interface : HttpMessageConverter

- 기능

  - @RequestBody, @ResponseBody가 있다면<br>**canRead() , canWrite()** : 메시지 컨버터가 해당 클래스, **미디어타입**을 지원하는지 체크 
  - boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);
  - boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);
  - **read() , write() : 메시지 컨버터를 통해서 메시지를 읽고 쓰는 기능**
    - T read(Class<? extends T> clazz, HttpInputMessage inputMessage)
    - void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage);

- Tomcat작동 시 RequestMappingHandlerAdaptor에 변수 등록 되는 부분

  - ```java
    // WebMvcConfigurationSupport.java
    adapter.setMessageConverters(getMessageConverters());
    
    // WebMvcConfigurationSupport.getMessageConverters
    protected final List<HttpMessageConverter<?>> getMessageConverters() {
       ... 생략 ...
      addDefaultHttpMessageConverters(this.messageConverters);
       ... 생략 ...
    }
    
    // WebMvcConfigurationSupport addDefaultHttpMessageConverters
    messageConverters.add(new ByteArrayHttpMessageConverter());
    messageConverters.add(new StringHttpMessageConverter());
    messageConverters.add(new MappingJackson2HttpMessageConverter(builder.build()));
    ```

- 구현체

  ```
  ByteArrayHttpMessageConverter : byte[] 데이터를 처리한다.
  	- 클래스 타입: byte[] , 미디어타입: */* ,                                           
    - 요청 예) @RequestBody byte[] data
    - 응답 예) @ResponseBody return byte[] 쓰기 미디어타입 application/octet-stream
  
  StringHttpMessageConverter : String 문자로 데이터를 처리한다. 클래스 타입: String , 미디어타입: */*
  	- 요청 예) @RequestBody String data
  	- 응답 예) @ResponseBody return "ok" 쓰기 미디어타입 text/plain
  
  MappingJackson2HttpMessageConverter : application/json
  	- 클래스 타입: 객체 또는 HashMap , 미디어타입 application/json 관련
  	- 요청 예) @RequestBody HelloData data
  	- 응답 예) @ResponseBody return helloData 쓰기 미디어타입 application/json 관련
  ```

- HTTP 메시지 컨버터는 HTTP 요청, HTTP 응답 둘 다 사용된다.



2. **argumentResolvers**

- Interface : HandlerMethodArgumentResolver

- 기능
  - boolean **supportsParameter**(MethodParameter parameter); 
    - 해당 파라미터를 지원하는지 체크
  - Object **resolveArgument**(...);
    - 지원하면 resolveArgument() 를 호출해서 실제 객체를 생성한다. <br>그리고 이렇게 생성된 객체가 컨트롤러 호출시 넘어가는 것이다.<br>이 안에 MessageConverter가 있다.

  - 구현체
    - RequestParamMethodArgumentResolver
    - RequestResponseBodyMethodProcessor

3. **returnValueHandlers**

- HandlerMethodReturnValueHandler

<br>


### 나. RequestMappingHandlerMapping을 생성하여 스프링빈에 등록한다.

```java
public RequestMappingHandlerMapping requestMappingHandlerMapping( ... 생략 ... ) {
  RequestMappingHandlerMapping mapping = createRequestMappingHandlerMapping();
}
```



-----


<br>


## 1. Client의 첫 요청 시 DispatcherServlet객체를 생성한 후 요청을 처리한다.

DispatcherServlet은 생성하고 초기화작업을 매번 하면 많은 작업비용이 발생하므로 <br>Client요청이 처음 있을 때에만 초기화 하고<br>이후 요청에는 이전에 만들어 졌던 DispatcherServlet을 재사용합니다. 

​	==> **싱글톤**으로 활용

<img width="442" alt="image-20220410202915181" src="https://user-images.githubusercontent.com/58017318/162986428-f654bdbf-4a7c-4881-a853-45cddfa6271c.png">


> https://victorydntmd.tistory.com/154



Servlet 생성 실행 시 init() 메소드를 사용하여 초기화한다.<br>이때, 아래 동작이 실행된다.

```java
// DispatcherServlet.class
protected void initStrategies(ApplicationContext context) {
  initMultipartResolver(context);
  initLocaleResolver(context);
  initThemeResolver(context);
  initHandlerMappings(context); // handlerMapping
  initHandlerAdapters(context); // handlerAdapter
  initHandlerExceptionResolvers(context);
  initRequestToViewNameTranslator(context);
  initViewResolvers(context);  // viewResolver
  initFlashMapManager(context);
}
```

> initStrategies는 다양한 인터페이스들의 초기화 메서드들이 존재한다. <br>이 중에서도 강좌에서 배운 handlerMapping, handlerAdapter, viewResolver가 보인다.

- 가. initHandlerMappings(context); 

  - 요청이 들어왔을 때 요청을 처리할 수 있는 핸들러를 찾아주는 인터페이스

  - ```java
    // DispatcherServlet.class
    private void initHandlerMappings(ApplicationContext context) {
      this.handlerMappings = null;
    
      if (this.detectAllHandlerMappings) {
        // ApplicationContext에서 HandlerMapping.class를 상속받은 클래스들을 찾는다.
        // Tomcat 구동 시 스프링빈에 올라간 HandlerMapping을 모두 찾는다
        Map<String, HandlerMapping> matchingBeans =
          BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        
        if (!matchingBeans.isEmpty()) {
          // this.handlerMappings 변수에 등록한다.
          this.handlerMappings = new ArrayList<>(matchingBeans.values());
          AnnotationAwareOrderComparator.sort(this.handlerMappings);
        }
      }
    ```

      - <img width="831" alt="image-20220410210929530" src="https://user-images.githubusercontent.com/58017318/162986832-d2642cc7-bb84-477d-9d33-f50b6f58ff9c.png">
    - 구현체
      - **BeanNameUrlHandlerMapping**
      - BeanNameUrlHandlerMapping

- 나. initHandlerAdapters(context);

  - 각각의 핸들러를 처리할 수 있는 interface
  - <img width="925" alt="image-20220410211109824" src="https://user-images.githubusercontent.com/58017318/162986964-5888da1a-be1a-418b-9a36-fa29cc14095d.png">
    - 구현체
      - **RequestMappingHandlerAdapter**
      - SimpleControllerHandlerAdapter 등등

- 다. initViewResolvers(context);

  - ViewName에 해당하는 뷰를 찾아내는 interface
  - <img width="920" alt="image-20220410211231078" src="https://user-images.githubusercontent.com/58017318/162987151-e8eb273d-4c66-42b1-8385-26ebbcb455bf.png">


> 자세한 내용은 https://ncucu.me/10 참조



------


<br>

## 2. Servlet에서 doDispatch() 함수를 통해 원하는<br> Url의 Controller를 찾아간다.

여기서부터는 Client의 요청이 들어올 때마다 실행되는 흐름이겠죠?
<img width="711" alt="image-20220411205927236" src="https://user-images.githubusercontent.com/58017318/162986539-891067f2-4383-4195-9be5-8402268bff5f.png">



저번시간에 헷갈렸던 ArguementResolver와 MessageConverter의 내용이나옵니다.<br>큰 흐름을 말씀드리면 hadler를 실행한 후 ArgumentResolver를 통해 MessageConverter를 실행하여 <br>해당 Controller의 Argument를 변환하여 반환해줍니다.

<img width="1162" alt="image-20220328203728863" src="https://user-images.githubusercontent.com/58017318/162986695-fd667d5d-9342-4955-a040-4270f658dc62.png">



### 가. DispatcherServlet이 handler, adapter를 찾는다.

```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
  // com.requestbody.requestbody.controller.RequestController#test(Hello)
  mappedHandler = getHandler(processedRequest);
  
  // RequestMappingHandlerAdapter@5407
  HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
  
  // handle을 통해 handler 접근
  mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

- @RequestMapping을 사용하였기에

  - handler: **RequestMappingHandlerMapping**를 통해 handler를 찾는다

    - **DispatcherServlet - doDispatch**
    - <img width="967" alt="image-20220403162300277" src="https://user-images.githubusercontent.com/58017318/162987341-5be46663-def6-4f0a-b9ef-6d02fdb30ed4.png">
  - Adapter: **RequestMappingHandlerAdapter**

    - <img width="1071" alt="image-20220403165701497" src="https://user-images.githubusercontent.com/58017318/162987455-e8057656-967c-467d-9523-266d3ad8e641.png">



### 나. Adapter.handle메소드를 실행한다.

```java
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

> HandlerAdapter ha.handle를 interface로 선언함으로써 <br>SOLID원칙 
>
> - **OCP: 개방-폐쇄 원칙(Open/closed principle)**
>
>   - HandlerAdapter interface는 이를 상속받은 Class **확장에는 열려** 있고<br> **변경에는 닫혀** 있어야 한다. ==> **다형성**
>
>   - interface로 작성하여 구현체를 유연하게 받을 수 있도록 설계된것이다.
> - DIP: 제어의역전 원칙
>
>   - **변화하지 않는 interface에 의존**하고있다.
>
> <img width="438" alt="image-20220328211049852" src="https://user-images.githubusercontent.com/58017318/162987558-cada7051-765f-4040-9c2b-4eea1c41793a.png">
>
>  @RequestMapping의 경우 HandlerAdaper를 상속받은 **RequestMappingHandlerAdaper**를 사용한다.
>
> > HandlerAdapter
> > **0 = RequestMappingHandlerAdapter**
> > : 애노테이션 기반의 컨트롤러인
> >   @RequestMapping에서 사용
> >
> > **1 = HttpRequestHandlerAdapter**
> > : HttpRequestHandler 처리
> >
> > **2 = SimpleControllerHandlerAdapter**
> > : Controller 인터페이스
> >   (애노테이션X, 과거에 사용) 처리



RequestMappingHandlerAdapter로 가보면 handle메소드를 찾아가보자

```java
@Override
@Nullable
public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
  throws Exception {

  return handleInternal(request, response, (HandlerMethod) handler);
}

// handleInternal 타고 
protected ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
		mav = this.invokeHandlerMethod(request, response, handlerMethod);
}

// invokeHandlerMethod 타고 들어가보면
```



```java
@Nullable
protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                           HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
		
  	
    if (this.argumentResolvers != null) {
      // invocableMethod에 argumentResolver 셋팅
      invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
    }
    if (this.returnValueHandlers != null) {
      invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
    }
  	
  	//// 생략 /// 
 		invocableMethod.invokeAndHandle(webRequest, mavContainer, new Object[0]);

//  ServletInvocableHandlerMethod.invokeAndHandle
	public void invokeAndHandle( ... 생략 ... ) throws Exception {

// InvocableHandlerMethod.invokeForRequest
    	public Object invokeForRequest( ... 생략 ... ) throws Exception {
		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);

```



-------



## 2. invokeForRequest 를 통해 Object를 리턴해준다.<br>

- ArguementResolver를 이용하여 Controller에 맞는 Argument Object 리턴하기

- ```java
  // ServletInvocableHandlerMethod.invokeAndHandle
  // retrun을 Object로 하여 해당 Controller에 원하는 Argument를 return시켜준다.
  Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
  ```

  

resolver Interface: **HandlerMethodArgumentResolver**

- ```java
  public interface HandlerMethodArgumentResolver {
    // 1. 해당 파라미터를 지원해주는 ArgumentResolver가 있는지 확인  
    boolean supportsParameter(MethodParameter parameter);
  	
    // 2. [1]에서 확인한 ArgumentResolver를 실행하여
    // 해당 Controller에서 원하는 Argument를 Return
    // messageConverter 을 사용해서 @RequestBody 파라미터를 받을 수 있게 도와줌
    Object resolveArgument( ... 생략 ...) throws Exception;
  }
  ```

- 구현체 : **RequestResponseBodyMethodProcessor**

- @RequestBody Hello

- <img width="712" alt="image-20220411185656927" src="https://user-images.githubusercontent.com/58017318/162987750-fb62f719-eff5-490d-8338-831063eaf23b.png">

  

```java
// InvocableHandlerMethod.getMethodArgumentValues
protected Object[] getMethodArgumentValues( ... 생략 ...) throws Exception {
	
// 가. supportsParameter에서 resolvers가 지원해주는 파라미터인지 확인한다.
  if (!this.resolvers.supportsParameter(parameter)) {
    throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
  }
  
// 나. ArgumentResolver를 실행하여 -- RequestResponseBodyMethodProcessor
//    해당 Controller에 맞는 Argument를 Return 해준다.
  try {
    args[i] = this.resolvers.resolveArgument(
      parameter, mavContainer, request, this.dataBinderFactory);
  }
}
```



### 가. supportsParameter에서 resolvers가 지원해주는 파라미터인지 확인한다.

 - resolver가 제공해주는지 확인

   - ```java
     public boolean supportsParameter(MethodParameter parameter) {
       // 첫번째. HandlerMethodArgumentResolver 에서 상속받은 [supportsParameter] 안에서 실행
       // RequestResponseBodyMethodProcessor.supportsParameter
       return getArgumentResolver(parameter) != null;
     }
     ```

     - <img width="710" alt="image-20220410234646866" src="https://user-images.githubusercontent.com/58017318/162987866-83c87366-8328-43cb-81a8-bfcf4e72258b.png">



### 나. 해당 Controller의 Argument에 맞는 ArgumentResolver를 실행하여 <br>해당 Controller에 맞는 Argument를 Return 해준다.

- ​	resolveArgument

  - ```java
    // Argument는 여러개일 수 있으니까 args[i]로 배열로 담는다.
    args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
    
    // this.resolvers.resolveArgument
    public Object resolveArgument( ... ) throws Exception {
      // 1. resolver를 가져온다.
      HandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
      
      // 2. resolver를 실행한다.
      // 두번째. HandlerMethodArgumentResolver 에서 상속받은 [resolveArgument] 안에서 실행하여
      // Argument Object를 반환
      return resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
    }
    
    ```

-----

<br>
<br>

## 3. ArgumentResolver.resolveArgument를 실행시켜 Object 구하기

구현체인 RequestResponseBodyMethodProcessor.class 안에 있는 resolveArgument를 실행하여 Object를 구한다.



```java
public Object resolveArgument(...) throws Exception {
	// 가. Argument와 매칭되는 MessageConverter를 확인하여
	// Argument의 Object를 Return 한다.
	Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
   ... 생략 ...
	return adaptArgumentIfNecessary(arg, parameter);
}

// readWithMessageConverters
protected <T> Object readWithMessageConverters( ... ) throws Exception {
  for (HttpMessageConverter<?> converter : this.messageConverters) {
    
    // 첫번째. MessageConverter의 canRead
    // MappingJackson2HttpMessageConverter.canRead
    /**
        if (!canRead(mediaType)) {
          return false;
        }
    **/
   	if (converter.canRead(targetClass, contentType) {
      // 
      body = converter.read(targetClass, msgToUse)
    }

  }
}

```



### 가. readWithMessageConverters를 통해 Argument의 Object를 Return 한다.

**HttpMessageConverter란?**

```java
public interface HttpMessageConverter<T> {
  // 첫번째 Argument의 메시지 컨버터가 
  // 해당 클래스, 미디어타입을 지원하는지 체크
  boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);
  boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);

  List<MediaType> getSupportedMediaTypes();
  
  // 메시지 컨버터를 통해서 메시지를 읽고 Object 전달
  T read(Class<? extends T> class, HttpInputMessage inputMessage) 
    throws IOException, HttpMessageNotReadableException;
  
  void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
    throws IOException, HttpMessageNotWritableException;
}
```



위와 같은 기능을 한다. 

소스에 들어가보자면

<img width="684" alt="image-20220412201725757" src="https://user-images.githubusercontent.com/58017318/162987984-eb644d6c-373a-47e7-8b32-2942305fd2b2.png">

AbstractMessageConverterMethodArgumentResolver 는 RequestResponseBodyMethodProcessor를 상속해주고<br>HandlerMethodArgumentResolver를 상속받았다.<br>즉, ArgumentResolver안에서 MessageConvertor를 선택하고

##### AbstractMessageConverterMethodArgumentResolver.readWithMessageConverters

```java
for (HttpMessageConverter<?> converter : this.messageConverters) {
  // 1. for문을 돌려 알맞은 MessageConverter선택
  GenericHttpMessageConverter<?> genericConverter =
    (converter instanceof GenericHttpMessageConverter ? (GenericHttpMessageConverter<?>) converter : null);
  
  // 2. 선택된 MessageConverter.canRead를 통해 
  // 해당 클래스, 미디어타입을 지원하는지 체크
  if (converter.canRead(targetClass, contentType))) {
    if (message.hasBody()) {
      
      // 3. Argument 데이터를 읽어들인다
      converter.read(targetClass, msgToUse);
      body = getAdvice().afterBodyRead(body, msgToUse, parameter, targetType, converterType);
    }
  }
}
return body;
```

-------

참고자료 : 
- http://terasolunaorg.github.io/guideline/1.0.1.RELEASE/en/Overview/SpringMVCOverview.html#id2
- https://victorydntmd.tistory.com/154


