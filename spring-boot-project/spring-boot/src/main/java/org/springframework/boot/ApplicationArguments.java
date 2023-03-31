/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.List;
import java.util.Set;

/**
 * 提供对用于运行SpringApplication的参数的访问
 * Provides access to the arguments that were used to run a {@link SpringApplication}.
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public interface ApplicationArguments {

	/**
	 * 返回传递给应用程序的原始未处理参数
	 * Return the raw unprocessed arguments that were passed to the application.
	 *
	 * @return the arguments
	 */
	String[] getSourceArgs();

	/**
	 * 返回所有选项参数的名称。例如，如果参数为“-foo=bar--debug”，则返回值[“foo”，“debug”]。
	 * Return the names of all option arguments. For example, if the arguments were
	 * "--foo=bar --debug" would return the values {@code ["foo", "debug"]}.
	 *
	 * @return the option names or an empty set
	 */
	Set<String> getOptionNames();

	/**
	 * 返回从参数解析的选项参数集是否包含具有给定名称的选项
	 * Return whether the set of option arguments parsed from the arguments contains an
	 * option with the given name.
	 *
	 * @param name the name to check
	 * @return {@code true} if the arguments contain an option with the given name
	 */
	boolean containsOption(String name);

	/**
	 * 返回与具有给定名称的arguments选项关联的值集合
	 * <p>
	 * 1. 如果该选项存在且没有参数（例如：“--foo”），则返回一个空集合（[]）
	 * <p>
	 * 2. 如果该选项存在并且只有一个值（例如“--foo=bar”），则返回一个包含一个元素的集合（[“bar]”）
	 * <p>
	 * 3. 如果该选项存在且具有多个值（例如“--foo=bar--foo=baz”），则返回一个集合，该集合包含每个值的元素（[“bar”，“baz]”）
	 * <p>
	 * 4. 如果该选项不存在，则返回null
	 * <p>
	 * 作者：tianlanshennu
	 * 链接：https://www.jianshu.com/p/f4183bf5ff42
	 * 来源：简书
	 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
	 * Return the collection of values associated with the arguments option having the
	 * given name.
	 * <ul>
	 * <li>if the option is present and has no argument (e.g.: "--foo"), return an empty
	 * collection ({@code []})</li>
	 * <li>if the option is present and has a single value (e.g. "--foo=bar"), return a
	 * collection having one element ({@code ["bar"]})</li>
	 * <li>if the option is present and has multiple values (e.g. "--foo=bar --foo=baz"),
	 * return a collection having elements for each value ({@code ["bar", "baz"]})</li>
	 * <li>if the option is not present, return {@code null}</li>
	 * </ul>
	 *
	 * @param name the name of the option
	 * @return a list of option values for the given name
	 */
	List<String> getOptionValues(String name);

	/**
	 * 返回已分析的非选项参数集合
	 * Return the collection of non-option arguments parsed.
	 *
	 * @return the non-option arguments or an empty list
	 */
	List<String> getNonOptionArgs();

}
