package com.m12i.query.parse;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.m12i.code.parse.ParseException;

/**
 * 文字列として表現されたクエリをパースして解析済みクエリを生成するファクトリ・オブジェクト.
 * @param <E> 解析済みクエリによる検索対象となるコレクションの要素型
 */
public final class QueryFactory<E> {
	/**
	 * {@link Map}のコレクションのためのクエリ・ファクトリを生成する.
	 * @return ファクトリ・オブジェクト
	 */
	public static QueryFactory<Map<String, Object>> createMapQueryFactory() { 
		return new QueryFactory<Map<String,Object>>(new Accessor<Map<String, Object>>() {
				@Override
				public String accsess(Map<String, Object> elem, String prop) {
					return elem.containsKey(prop) ? elem.get(prop).toString() : null;
				}
			});
	}
	/**
	 * JavaBeansのコレクションのためのクエリ・ファクトリを生成する.
	 * @param elemType 検索対象コレクションの要素型
	 * @return ファクトリ・オブジェクト
	 */
	public static<T> QueryFactory<T> createBeanQueryFactory(final Class<T> elemType) { 
		// getterっぽいメソッドを格納しておくマップ
		final HashMap<String, Method> methods = new HashMap<String, Method>();
		try {
			// 引数で指定されたクラスのpublicメソッドについて繰り返し処理
			for (final Method m : elemType.getMethods()) {
				// メソッドのシグネチャをチェック
				if (m.getParameterTypes().length == 0 && m.getReturnType() != Void.class) {
					// 「引数の数がゼロであり かつ 戻り値がvoidでない」場合はgetterとみなす
					methods.put(m.getName(), m);
				}
			}
		} catch(Exception e) {
			// Do nothing.
		}
		// ファクトリを初期化して返す
		return new QueryFactory<T>(new Accessor<T>() {
			// getterをコールする際に使用するダミーの引数リスト
			private final Object[] args = new Object[0];
			// プロパティ名にマッチするgetterメソッドを返す
			private Method getGetter(String prop) {
				// JavaBeans規約に則ったgetter名を構成
				final String getterName = "get" +
						prop.substring(0, 1).toUpperCase() + prop.substring(1);
				// getterっぽいメソッドを格納したマップの要素キーと照合
				if (methods.containsKey(getterName)) {
					// JavaBeans規約に則ったgetterが存在すればそれを返す
					return methods.get(getterName);
				} else if (methods.containsKey(prop)) {
					// プロパティと同名のメソッドが存在すればそれを返す
					return methods.get(prop);
				} else {
					// いずれにも該当しなければnullを返す
					return null;
				}
			}
			@Override
			public String accsess(T elem, String prop) {
				try {
					// プロパティ名にマッチするメソッドを探す
					final Method m = getGetter(prop);
					// ダミーの引数リストに適用して結果を返す
					return m == null ? null : m.invoke(elem, args).toString();
				} catch (Exception e) {
					// 何らかの理由でメソッドコールが失敗したらともかくnullを返す
					return null;
				}
			}
		});
	}
	private static final ExpressionParser p = new ExpressionParser();
	private final Accessor<E> a;
	/**
	 * ファクトリ・オブジェクトのコンストラクタ.
	 * アクセサ・オブジェクト──クエリの条件式で指定されたプロパティを要素から取得するためのオブジェクト──をパラメータとして受け取り、
	 * ファクトリ・オブジェクトを初期化します。
	 * @param accessor アクセサ・オブジェクト
	 */
	public QueryFactory(Accessor<E> accessor) {
		if (accessor == null) {
			throw new IllegalArgumentException();
		}
		this.a = accessor;
	}
	/**
	 * 文字列として表現されたクエリをパースして解析済みクエリを生成する.
	 * @param query クエリ文字列
	 * @return 解析済みクエリ
	 * @throws QueryParseException クエリ内容に問題があり解析に失敗した場合
	 */
	public Query<E> create(String query) throws QueryParseException {
		try {
			return new QueryImpl<E>(p.parse(query), a);
		} catch (ParseException e) {
			throw new QueryParseException(e);
		}
	}
}
