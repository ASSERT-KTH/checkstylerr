package com.hlj.beanlife;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
/**
 * ,setApplicationContenxt完成之后开始调用
	配置自己的后置处理器让，让其它bean都是单例singoton, *
 */
public class MyBeanPostProcessor implements BeanPostProcessor {
	
	//这个先运行
		public Object postProcessBeforeInitialization(Object arg0, String arg1)
				throws BeansException {
			// TODO Auto-generated method stub
			System.out.printf("6、调用 BeanPostProcessor中的postProcessBeforeInitialization ----");
			System.out.println("对象"+arg0+" 被创建的时间是 "+new java.util.Date());

			return arg0;
		}
		
	public Object postProcessAfterInitialization(Object arg0, String arg1)
			throws BeansException {
		System.out.printf("9、调用 BeanPostProcessor中postProcessAfterInitialization -----");
		System.out.println("对象"+arg0+" 被创建的时间是 "+new java.util.Date());
		return arg0;
	}
	


}
