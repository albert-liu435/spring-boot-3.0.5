/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.util.Assert;

/**
 * ConfigurableBootstrapContext的默认实现类
 * 该类实现了ConfigurableBootstrapContext接口，因此具有ConfigurableBootstrapContext的所有功能。
 * Default {@link ConfigurableBootstrapContext} implementation.
 *
 * @author Phillip Webb
 * @since 2.4.0
 */
public class DefaultBootstrapContext implements ConfigurableBootstrapContext {

	//存放InstanceSupplier对象，该对象用来获取实际对象，也就是说用该对象创建实际对象
	private final Map<Class<?>, InstanceSupplier<?>> instanceSuppliers = new HashMap<>();

	//存放实际的单例对象
	private final Map<Class<?>, Object> instances = new HashMap<>();
//事件广播器

	private final ApplicationEventMulticaster events = new SimpleApplicationEventMulticaster();

	//向注册表注册特定类型。如果指定的类型已注册，但尚未作为单例获得，则将替换它。
	@Override
	public <T> void register(Class<T> type, InstanceSupplier<T> instanceSupplier) {
		register(type, instanceSupplier, true);
	}

	//如果尚未注册特定类型，请向注册表注册该类型。
	@Override
	public <T> void registerIfAbsent(Class<T> type, InstanceSupplier<T> instanceSupplier) {
		register(type, instanceSupplier, false);
	}

	//实际执行的注册方法
	private <T> void register(Class<T> type, InstanceSupplier<T> instanceSupplier, boolean replaceExisting) {
		Assert.notNull(type, "Type must not be null");
		Assert.notNull(instanceSupplier, "InstanceSupplier must not be null");
		//加锁，有可能有多个线程同时操作注册中心，而Map类型是线程不安全的，所以需要加锁
		synchronized (this.instanceSuppliers) {
			//判断instanceSuppliers 注册表中是否存在该类型
			boolean alreadyRegistered = this.instanceSuppliers.containsKey(type);
			//如果需要用新的值替换旧的值或者该类型在注册表中不存在，那么向注册表中注册该特定类型。
			if (replaceExisting || !alreadyRegistered) {
				Assert.state(!this.instances.containsKey(type), () -> type.getName() + " has already been created");
				this.instanceSuppliers.put(type, instanceSupplier);
			}
		}
	}

	// 如果给定类型存在注册，则返回true,否则返回false
	@Override
	public <T> boolean isRegistered(Class<T> type) {
		synchronized (this.instanceSuppliers) {
			//判断指定类型是否存在
			return this.instanceSuppliers.containsKey(type);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> InstanceSupplier<T> getRegisteredInstanceSupplier(Class<T> type) {
		synchronized (this.instanceSuppliers) {
			return (InstanceSupplier<T>) this.instanceSuppliers.get(type);
		}
	}

	@Override
	public void addCloseListener(ApplicationListener<BootstrapContextClosedEvent> listener) {
		this.events.addApplicationListener(listener);
	}

	@Override
	public <T> T get(Class<T> type) throws IllegalStateException {
		return getOrElseThrow(type, () -> new IllegalStateException(type.getName() + " has not been registered"));
	}

	@Override
	public <T> T getOrElse(Class<T> type, T other) {
		return getOrElseSupply(type, () -> other);
	}

	@Override
	public <T> T getOrElseSupply(Class<T> type, Supplier<T> other) {
		synchronized (this.instanceSuppliers) {
			InstanceSupplier<?> instanceSupplier = this.instanceSuppliers.get(type);
			return (instanceSupplier != null) ? getInstance(type, instanceSupplier) : other.get();
		}
	}

	@Override
	public <T, X extends Throwable> T getOrElseThrow(Class<T> type, Supplier<? extends X> exceptionSupplier) throws X {
		synchronized (this.instanceSuppliers) {
			InstanceSupplier<?> instanceSupplier = this.instanceSuppliers.get(type);
			if (instanceSupplier == null) {
				throw exceptionSupplier.get();
			}
			return getInstance(type, instanceSupplier);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getInstance(Class<T> type, InstanceSupplier<?> instanceSupplier) {
		T instance = (T) this.instances.get(type);
		if (instance == null) {
			instance = (T) instanceSupplier.get(this);
			if (instanceSupplier.getScope() == Scope.SINGLETON) {
				this.instances.put(type, instance);
			}
		}
		return instance;
	}


	// 发布关闭BootstrapContext并准备ApplicationContext的事件

// 方法在关闭BootstrapContext并准备ApplicationContext时调用。
	/**
	 * Method to be called when {@link BootstrapContext} is closed and the
	 * {@link ApplicationContext} is prepared.
	 *
	 * @param applicationContext the prepared context
	 */
	public void close(ConfigurableApplicationContext applicationContext) {
		this.events.multicastEvent(new BootstrapContextClosedEvent(this, applicationContext));
	}

}
