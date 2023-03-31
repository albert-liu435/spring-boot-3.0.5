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

/**
 * 功能:  该接口继承了BootstrapRegistry和BootstrapContext并且该接口没有定义其他方法，该接口是一个BootstrapContext,也通过BootstrapRegistry提供了相应的配置方法，也就是说该接口即使一个BootstrapContext又是一个BootstrapRegistry。
 * <p>
 * A {@link BootstrapContext} that also provides configuration methods through the
 * {@link BootstrapRegistry} interface.
 *
 * @author Phillip Webb
 * @see BootstrapRegistry
 * @see BootstrapContext
 * @see DefaultBootstrapContext
 * @since 2.4.0
 */
public interface ConfigurableBootstrapContext extends BootstrapRegistry, BootstrapContext {

}
