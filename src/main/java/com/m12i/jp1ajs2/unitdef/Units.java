package com.m12i.jp1ajs2.unitdef;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.m12i.jp1ajs2.unitdef.parser.ParseException;
import com.m12i.jp1ajs2.unitdef.parser.UnitParser;
import com.m12i.jp1ajs2.unitdef.util.Formatter;
import com.m12i.jp1ajs2.unitdef.util.Maybe;

/**
 * {@link Unit}のためのユーティリティを提供するオブジェクト.
 */
public final class Units {
	private static final UnitParser parser = new UnitParser();
	private static final Formatter formatter = new Formatter();
	private static final UnitCollector collector = new UnitCollector();
	
	private Units() {}

	/**
	 * ファイルからユニット定義情報を読み取る.
	 * システム・デフォルトのキャラクターセットを使用する。
	 * @param f ファイル
	 * @return ユニット定義
	 * @throws IllegalArgumentException 構文エラーが検出された場合
	 */
	public static Unit fromFile(final File f) {
		return parser.parse(f);
	}

	/**
	 * ファイルからユニット定義情報を読み取る.
	 * @param f ファイル
	 * @param charset キャラクターセット
	 * @return ユニット定義
	 * @throws IllegalArgumentException 構文エラーが検出された場合
	 */
	public static Unit fromFile(final File f, final Charset charset) {
		return parser.parse(f, charset);
	}

	/**
	 * 入力ストリームからユニット定義情報を読み取る.
	 * システム・デフォルトのキャラクターセットを使用する。
	 * @param s ストリーム
	 * @return ユニット定義
	 * @throws IOException I/Oエラーが発生した場合
	 * @throws IllegalArgumentException 構文エラーが検出された場合
	 */
	public static Unit fromStream(final InputStream s) throws IOException {
		return parser.parse(s);
	}

	/**
	 * 入力ストリームからユニット定義情報を読み取る.
	 * @param s ストリーム
	 * @param charset キャラクターセット
	 * @return ユニット定義
	 * @throws IOException I/Oエラーが発生した場合
	 * @throws IllegalArgumentException 構文エラーが検出された場合
	 */
	public static Unit fromStream(final InputStream s, final Charset charset) throws IOException {
		return parser.parse(s, charset);
	}

	/**
	 * 文字列からユニット定義情報を読み取る.
	 * @param s 文字列
	 * @return ユニット定義
	 * @throws IllegalArgumentException 構文エラーが検出された場合
	 */
	public static Unit fromString(final String s) {
		try {
			return parser.parse(s);
		} catch (final ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * 引数に指定されたユニット定義とその子孫のユニット定義を要素とするリストを返す.
	 * リスト要素は深さ優先探索により発見された順序で並ぶ。
	 * @param unit ユニット
	 * @return ユニット定義リスト
	 */
	public static List<Unit> asList(Unit unit) {
		return collector.collect(unit);
	}
	
	/**
	 * ユニット定義パラメータを検索して返す.
	 * @param unit ユニット定義
	 * @param paramName ユニット定義パラメータ名
	 * @return ユニット定義パラメータを要素とする{@link Maybe}
	 */
	public static Maybe<Param> getParams(final Unit unit, final String paramName) {
		final List<Param> list = new ArrayList<Param>();
		for (final Param p : unit.getParams()) {
			if (paramName.equals(p.getName())) {
				list.add(p);
			}
		}
		return Maybe.wrap(list);
	}

	/**
	 * 子にあたるユニット定義を検索して返す.
	 * @param unit ユニット定義 
	 * @param unitName ユニット名
	 * @return ユニット定義を要素とする{@link Maybe}
	 */
	public static Maybe<Unit> getSubUnits(final Unit unit, final String unitName) {
		final List<Unit> list = new ArrayList<Unit>();
		for (final Unit u : unit.getSubUnits()) {
			if (unitName.equals(u.getName())) {
				list.add(u);
			}
		}
		return Maybe.wrap(list);
	}
	
	/**
	 * 子孫のユニット定義を検索して返す.
	 * @param unit ユニット定義
	 * @param unitName ユニット名
	 * @return ユニット定義を要素とする{@link Maybe}
	 */
	public static Maybe<Unit> getDescendentUnits(final Unit unit, final String unitName) {
		return Maybe.wrap(new DescendentUnitsCollector(unitName).collect(unit));
	}
	
	/**
	 * ユニット定義を文字列化する.
	 * @param unit ユニット定義
	 * @return 文字列化されたユニット定義
	 */
	public static String toString(final Unit unit) {
		return formatter.format(unit);
	}
	
	/**
	 * ユニット定義を文字列化して出力ストリームに書き出す.
	 * @param unit ユニット定義
	 * @param out 出力ストリーム
	 * @param charset キャラクターセット
	 * @throws IOException 処理中にI/Oエラーが発生した場合
	 */
	public static void toStream(final Unit unit, final OutputStream out, final Charset charset) throws IOException {
		formatter.format(unit, out, charset);
	}
	
	/**
	 * ユニット定義を文字列化して出力ストリームに書き出す.
	 * @param unit ユニット定義
	 * @param out 出力ストリーム
	 * @throws IOException 処理中にI/Oエラーが発生した場合
	 */
	public static void toStream(final Unit unit, final OutputStream out) throws IOException {
		toStream(unit, out, Charset.defaultCharset());
	}
}