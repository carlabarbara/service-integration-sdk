/*
 * Copyright 2017 AppDirect, Inc. and/or its affiliates
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdirect.sdk.support;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.assertj.core.api.Condition;

/**
 * Verifies that a utility class is well-defined (according to Sonar): final class, private ctor.
 * Lifted from http://stackoverflow.com/a/10872497/26605
 */
public class UtilityClassesAreWellDefined {
	public static void verify(final Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		assertThat(isFinal(clazz.getModifiers())).withFailMessage("class must be final").isTrue();
		assertThat(clazz.getDeclaredConstructors().length).withFailMessage("There must be only one constructor").isEqualTo(1);

		final Constructor<?> constructor = clazz.getDeclaredConstructor();
		assertThat(constructor).is(privateConstructor());

		callPrivateConstructor(constructor);

		assertThat(clazz.getMethods()).allMatch(staticMethod(clazz));
	}

	private static Condition<? super Constructor<?>> privateConstructor() {
		return new Condition<>((c) -> !c.isAccessible() && isPrivate(c.getModifiers()), "a private constructor");
	}

	private static Predicate<? super Method> staticMethod(Class<?> clazz) {
		return (m) -> !(!isStatic(m.getModifiers()) && m.getDeclaringClass().equals(clazz));
	}

	private static void callPrivateConstructor(Constructor<?> constructor) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		constructor.setAccessible(true);
		constructor.newInstance();
		constructor.setAccessible(false);
	}
}
