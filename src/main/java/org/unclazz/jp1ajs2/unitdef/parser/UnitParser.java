package org.unclazz.jp1ajs2.unitdef.parser;

import java.util.Arrays;
import java.util.List;

import org.unclazz.jp1ajs2.unitdef.Parameter;
import org.unclazz.jp1ajs2.unitdef.ParameterValue;
import org.unclazz.jp1ajs2.unitdef.Attributes;
import org.unclazz.jp1ajs2.unitdef.FullQualifiedName;
import org.unclazz.jp1ajs2.unitdef.Tuple;
import org.unclazz.jp1ajs2.unitdef.Unit;
import org.unclazz.jp1ajs2.unitdef.builder.Builders;
import org.unclazz.jp1ajs2.unitdef.builder.TupleBuilder;
import org.unclazz.jp1ajs2.unitdef.util.CharSequenceUtils;
import org.unclazz.jp1ajs2.unitdef.util.ListUtils;
import static org.unclazz.jp1ajs2.unitdef.util.ListUtils.*;

public final class UnitParser extends ParserSupport<List<Unit>> {
	private static final ParseOptions ParseOptions = new ParseOptions();
	static {
		ParseOptions.setEscapePrefixInDoubleQuotes('#');
	}
	
	public UnitParser() {
		super(ParseOptions);
	}
	
	public ParseResult<List<Unit>> parse(final Input in) {
		final List<Unit> ret = arrayList();
		while (!in.reachedEOF()) {
			try {
				helper.skipWhitespace(in);
				ret.add(parseUnit(in, null));
				helper.skipWhitespace(in);
			} catch (final ParseException e) {
				return ParseResult.failure(e);
			}
		}
		if (ret.isEmpty()) {
			return ParseResult.failure(new IllegalArgumentException("Unit definition is not found."));
		}
		return ParseResult.successful(ListUtils.immutableList(ret));
	}
	
	Unit parseUnit(final Input in, final FullQualifiedName parent) throws ParseException {
		try {
			// ユニット定義の開始キーワードを読み取る
			helper.skipWhitespace(in);
			helper.skipWord(in, "unit");
	
			// ユニット定義属性その他の初期値を作成
			final List<String> attrList = Arrays.asList("", "", "", "");
			final List<Parameter> params = linkedList();
	
			// ユニット定義属性を読み取る
			// 属性は最大で4つ、カンマ区切りで指定される
			for (int i = 0; i < 4; i++) {
				in.next();
				attrList.set(i, parseAttr(in));
				
				// 現在文字をチェック
				if (in.current() == ';') {
					// ';'である場合、ユニット属性パラメータは終わり
					break;
				}
			}
			final Attributes attrs = Builders
					.attributes()
					.setName(attrList.get(0))
					.setPermissionMode(Builders.permissionMode(attrList.get(1)))
					.setJP1UserName(attrList.get(2))
					.setResourceGroupName(attrList.get(3))
					.build();
			
			final FullQualifiedName fqn = (parent == null) 
					? Builders.fullQualifiedName().addFragment(attrs.getUnitName()).build()
					: parent.getSubUnitName(attrs.getUnitName());
	
			// 属性の定義は「；」で終わる
			helper.check(in, ';');
	
			in.next();
			helper.skipWhitespace(in);
	
			// ユニット定義パラメータの開始カッコを読み取る
			helper.check(in, '{');
			in.next();
			
			// 空白をスキップ
			helper.skipWhitespace(in);
	
			// '}'が登場したらそこでユニット定義は終わり
			if (in.current() == '}') {
				// しかしユニット定義にはすくなくともtyパラメータは必要
				throw new IllegalArgumentException("parameter \"ty\" is not found");
			}
	
			// サブユニットを格納するリストを初期化
			final List<Unit> subUnits = linkedList();
			
			// "unit"で始まらないならそれはパラメータ
			if(! in.restStartsWith("unit")){
				while (in.unlessEOF()) {
					// パラメータを読み取る
					params.add(parseParam(in));
					// パラメータ読み取り後にもかかわらず現在文字が';'でないなら構文エラー
					helper.check(in, ';');
					in.next();
					helper.skipWhitespace(in);
					
					// '}'が登場したらそこでユニット定義は終わり
					if (in.current() == '}') {
						in.next();
						return Builders
								.unit()
								.setFullQualifiedName(fqn)
								.setAttributes(attrs)
								.addParameters(params)
								.build();
						
					/// "unit"と続くならパラメータの定義は終わりサブユニットの定義に移る
					}else if(in.restStartsWith("unit")){
						break;
					}
				}
			}
			
			// "unit"で始まるならそれはサブユニット
			while (in.restStartsWith("unit")) {
				subUnits.add(parseUnit(in, fqn));
				helper.skipWhitespace(in);
			}
			
			helper.check(in, '}');
			in.next();
			return Builders
					.unit()
					.setFullQualifiedName(fqn)
					.setAttributes(attrs)
					.addParameters(params)
					.addSubUnits(subUnits)
					.build();
		} catch (InputExeption e) {
			throw new ParseException(e, in);
		}
	}

	Parameter parseParam(final Input in) throws ParseException {
		try {
			// '='より以前のパラメータ名の部分を取得する
			final String name = helper.parseUntil(in, '=');
			// パラメータ名が存在しない場合は構文エラー
			if (name.length() == 0) {
				throw ParseException.syntaxError(in);
			}
			// パラメータ値を一時的に格納するリストを初期化
			final List<ParameterValue> values = arrayList();
			// パラメータの終端文字';'が登場するまで繰り返し
			while (in.current() != ';') {
				// '='や','を読み飛ばして前進
				in.next();
				// パラメータ値を読み取っていったんリストに格納
				values.add(parseParamValue(in));
				// パラメータ値読取り後にもかかわらず現在文字が区切り文字以外であれば構文エラー
				if (in.current() != ',' && in.current() != ';') {
					throw ParseException.syntaxError(in);
				}
			}
			// 読取った結果を使ってパラメータを初期化して返す
			return Builders
					.parameter()
					.setName(name)
					.addValues(values)
					.build();
		} catch (InputExeption e) {
			throw new ParseException(e, in);
		}
	}
	
	ParameterValue parseParamValue(final Input in) throws ParseException {
		switch (in.current()) {
		case '(':
			final Tuple t = parseTuple(in);
			return Builders.tupleParameterValue(t);
		case '"':
			final String q = helper.parseQuotedString(in);
			return Builders.quotedParameterValue(q);
		default:
			final String s = parseRawString(in);
			return Builders.charSequenceParameterValue(s);
		}
	}

	String parseRawString(final Input in) throws ParseException {
		try {
			final StringBuilder sb = CharSequenceUtils.builder();
			while (in.unlessEOF()) {
				final char c = in.current();
				if (c == ',' || c == ';') {
					break;
				} else if (c == '"') {
					final String quoted = helper.parseQuotedString(in);
					sb.append('"').append(quoted.replaceAll("#", "##").replaceAll("\"", "#\"")).append('"');
				} else {
					sb.append(c);
					in.next();
				}
			}
			return sb.toString();
		} catch (InputExeption e) {
			throw new ParseException(e, in);
		}
	}
	
	Tuple parseTuple(final Input in) throws ParseException {
		try {
			helper.check(in, '(');
			final TupleBuilder builder = Builders.tuple();
			in.next();
			while (in.unlessEOF() && in.current() != ')') {
				final StringBuilder sb0 = CharSequenceUtils.builder();
				final StringBuilder sb1 = CharSequenceUtils.builder();
				boolean hasKey = false;
				while (in.unlessEOF() && (in.current() != ')' && in.current() != ',')) {
					if (in.current() == '=') {
						hasKey = true;
						in.next();
					}
					(hasKey ? sb1 : sb0).append(in.current());
					in.next();
				}
				if (hasKey) {
					builder.add(sb0.toString(), sb1.toString());
				} else {
					builder.add(sb0.toString());
				}
				if (in.current() == ')') {
					break;
				}
				in.next();
			}
			helper.check(in, ')');
			in.next();
			return builder.build();
		} catch (InputExeption e) {
			throw new ParseException(e, in);
		}
	}
	
	String parseAttr(final Input in) throws ParseException {
		try {
			final StringBuilder sb = CharSequenceUtils.builder();
			while(in.unlessEOF()) {
				final char c = in.current();
				if(c == ',' || c == ';') {
					return sb.length() == 0 ? null : sb.toString();
				}
				sb.append(c);
				in.next();
			}
			throw ParseException.syntaxError(in);
		} catch (InputExeption e) {
			throw new ParseException(e, in);
		}
	}
}
