/*
 * Copyright 2012-2023 the original author or authors.
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

import java.util.function.Supplier;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * 该接口是一个简单的对象注册表，在启动和 Environment 后处理期间可用，直到准备好ApplicationContext为止。可以用于注册创建成本较高或需要在ApplicationContext可用之前共享的实例。注册表使用Class作为键，这意味着只能存储给定类型的单个实例。
 * <p>
 * A simple object registry that is available during startup and {@link Environment}
 * post-processing up to the point that the {@link ApplicationContext} is prepared.
 * <p>
 * Can be used to register instances that may be expensive to create, or need to be shared
 * before the {@link ApplicationContext} is available.
 * <p>
 * The registry uses {@link Class} as a key, meaning that only a single instance of a
 * given type can be stored.
 * <p>
 * The {@link #addCloseListener(ApplicationListener)} method can be used to add a listener
 * that can perform actions when {@link BootstrapContext} has been closed and the
 * {@link ApplicationContext} is fully prepared. For example, an instance may choose to
 * register itself as a regular Spring bean so that it is available for the application to
 * use.
 *
 * @author Phillip Webb
 * @see BootstrapContext
 * @see ConfigurableBootstrapContext
 * @since 2.4.0
 */
public interface BootstrapRegistry {

	/**
	 * 向注册表注册特定类型。如果指定的类型已注册，但尚未作为单例获得，则将替换它。
	 * Register a specific type with the registry. If the specified type has already been
	 * registered and has not been obtained as a {@link Scope#SINGLETON singleton}, it
	 * will be replaced.
	 *
	 * @param <T>              the instance type
	 * @param type             the instance type
	 * @param instanceSupplier the instance supplier
	 */
	<T> void register(Class<T> type, InstanceSupplier<T> instanceSupplier);

	/**
	 * 如果尚未注册特定类型，请向注册表注册该类型。
	 * Register a specific type with the registry if one is not already present.
	 *
	 * @param <T>              the instance type
	 * @param type             the instance type
	 * @param instanceSupplier the instance supplier
	 */
	<T> void registerIfAbsent(Class<T> type, InstanceSupplier<T> instanceSupplier);

	/**
	 * 如果给定类型存在注册，则返回true,否则返回false
	 * Return if a registration exists for the given type.
	 *
	 * @param <T>  the instance type
	 * @param type the instance type
	 * @return {@code true} if the type has already been registered
	 */
	<T> boolean isRegistered(Class<T> type);

	/**
	 * 返回给定类型的任何现有BootstrapRegistry.InstanceSupplier
	 * Return any existing {@link InstanceSupplier} for the given type.
	 *
	 * @param <T>  the instance type
	 * @param type the instance type
	 * @return the registered {@link InstanceSupplier} or {@code null}
	 */
	<T> InstanceSupplier<T> getRegisteredInstanceSupplier(Class<T> type);

	/**
	 * 添加一个ApplicationListener，当BootstrapContext关闭且ApplicationContext已准备好时，将使用BootstrapContextClosedEvent调用它。
	 * <p>
	 * Add an {@link ApplicationListener} that will be called with a
	 * {@link BootstrapContextClosedEvent} when the {@link BootstrapContext} is closed and
	 * the {@link ApplicationContext} has been prepared.
	 *
	 * @param listener the listener to add
	 */
	void addCloseListener(ApplicationListener<BootstrapContextClosedEvent> listener);

	/**
	 * 该函数式接口的功能是: 在需要时提供实际实例
	 * Supplier used to provide the actual instance when needed.
	 *
	 * @param <T> the instance type
	 * @see Scope
	 */
	@FunctionalInterface
	interface InstanceSupplier<T> {

		/**
		 * 工厂方法，用于在需要时创建实例。
		 * Factory method used to create the instance when needed.
		 *
		 * @param context the {@link BootstrapContext} which may be used to obtain other
		 *                bootstrap instances.
		 * @return the instance
		 */
		T get(BootstrapContext context);

		/**
		 * 返回所提供实例的作用域,默认为单例
		 * Return the scope of the supplied instance.
		 *
		 * @return the scope
		 * @since 2.4.2
		 */
		default Scope getScope() {
			return Scope.SINGLETON;
		}

		/**
		 * 返回一个新的 BootstrapRegistry.InstanceSupplier，其中包含一个更新的BootstrapRegistry.Scope
		 * Return a new {@link InstanceSupplier} with an updated {@link Scope}.
		 *
		 * @param scope the new scope
		 * @return a new {@link InstanceSupplier} instance with the new scope
		 * @since 2.4.2
		 */
		default InstanceSupplier<T> withScope(Scope scope) {
			Assert.notNull(scope, "Scope must not be null");
			InstanceSupplier<T> parent = this;
			return new InstanceSupplier<>() {

				@Override
				public T get(BootstrapContext context) {
					return parent.get(context);
				}

				@Override
				public Scope getScope() {
					return scope;
				}

			};
		}

		/**
		 * 可用于为给定实例创建 BootstrapRegistry.InstanceSupplier的工厂方法
		 * Factory method that can be used to create an {@link InstanceSupplier} for a
		 * given instance.
		 *
		 * @param <T>      the instance type
		 * @param instance the instance
		 * @return a new {@link InstanceSupplier}
		 */
		static <T> InstanceSupplier<T> of(T instance) {
			return (registry) -> instance;
		}

		/**
		 * 可用于从Supplier创建BootstrapRegistry.InstanceSupplier的工厂方法
		 * Factory method that can be used to create an {@link InstanceSupplier} from a
		 * {@link Supplier}.
		 *
		 * @param <T>      the instance type
		 * @param supplier the supplier that will provide the instance
		 * @return a new {@link InstanceSupplier}
		 */
		static <T> InstanceSupplier<T> from(Supplier<T> supplier) {
			return (registry) -> (supplier != null) ? supplier.get() : null;
		}

	}

	/**
	 * 实例作用域枚举
	 * The scope of an instance.
	 *
	 * @since 2.4.2
	 */
	enum Scope {

		/**
		 * 单例实例。BootstrapRegistry.InstanceSupplier只会被调用一次，每次都会返回相同的实例。
		 * A singleton instance. The {@link InstanceSupplier} will be called only once and
		 * the same instance will be returned each time.
		 */
		SINGLETON,

		/**
		 * 原型实例。只要需要实例就会调用BootstrapRegistry.InstanceSupplier
		 * A prototype instance. The {@link InstanceSupplier} will be called whenever an
		 * instance is needed.
		 */
		PROTOTYPE

	}

}
