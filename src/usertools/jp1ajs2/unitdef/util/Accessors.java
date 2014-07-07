package usertools.jp1ajs2.unitdef.util;

import static usertools.jp1ajs2.unitdef.util.Helpers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import usertools.jp1ajs2.unitdef.core.Param;
import usertools.jp1ajs2.unitdef.core.ParamValue;
import usertools.jp1ajs2.unitdef.core.Tuple;
import usertools.jp1ajs2.unitdef.core.Unit;
import usertools.jp1ajs2.unitdef.core.ParamValue.ParamValueType;
import usertools.jp1ajs2.unitdef.ext.Arrow;
import usertools.jp1ajs2.unitdef.ext.ConnectorOrderingSyncOption;
import usertools.jp1ajs2.unitdef.ext.DeleteOption;
import usertools.jp1ajs2.unitdef.ext.Element;
import usertools.jp1ajs2.unitdef.ext.EvaluateConditionType;
import usertools.jp1ajs2.unitdef.ext.ExecutionUserType;
import usertools.jp1ajs2.unitdef.ext.HoldType;
import usertools.jp1ajs2.unitdef.ext.MailAddress;
import usertools.jp1ajs2.unitdef.ext.MapSize;
import usertools.jp1ajs2.unitdef.ext.ResultJudgmentType;
import usertools.jp1ajs2.unitdef.ext.UnitConnectionType;
import usertools.jp1ajs2.unitdef.ext.WriteOption;

public class Accessors {
	private Accessors(Unit unit) {}

	// For Collections
	private static final Pattern PARAM_EL_VALUE_3 = Pattern
			.compile("^\\+(\\d+)\\s\\+(\\d+)$");
	private static final Pattern PARAM_SZ_VALUE_1 = Pattern.compile("^(\\d+)[^\\d]+(\\d+)$");

	/**
	 * ユニット構成定義情報"el"で指定されたサブユニットの位置情報のリストを返す.
	 * サブユニットが存在しない場合は空のリストを返します。
	 * JP1定義コードでは、サブユニットの位置情報や関連線の情報はサブユニット自身では保持しておらず、
	 * 親ユニット側のユニット定義パラメータとして保持されている点に注意してください。
	 * 
	 * @return サブユニットの位置情報のリスト
	 */
	public static List<Element> elements(final Unit parentUnit) {
		final List<Element> result = new ArrayList<Element>();
		final List<Param> els = findParamAll(parentUnit, "el");
		for (final Param el : els) {
			Matcher m = PARAM_EL_VALUE_3.matcher(el.getValues().get(2)
					.getUnclassifiedValue());
			m.matches();
			final Unit unit = parentUnit.getSubUnit(el.getValues().get(0)
					.getUnclassifiedValue());
			final int horizontalPixel = Integer.parseInt(m.group(1));
			final int verticalPixel = Integer.parseInt(m.group(2));
			result.add(new Element(unit, horizontalPixel, verticalPixel));
		}
		return result;
	}
	public static MapSize size(Unit unit) {
		final Param sz = findParamOne(unit, "sz");
		final Matcher m = PARAM_SZ_VALUE_1.matcher(sz.getValue());
		m.matches();
		return new MapSize() {
			@Override
			public int getWidth() {
				return Integer.parseInt(m.group(1));
			}
			@Override
			public int getHeight() {
				return Integer.parseInt(m.group(2));
			}
		};
	}
	public static boolean jobnetConnectorOrdering(Unit unit) {
		final Param p = findParamOne(unit, "ncl");
		return p != null && p.getValue().equals("y");
	}
	
	public static String jobnetConnectorName(Unit unit) {
		final Param p = findParamOne(unit, "ncn");
		return p != null ? p.getValue() : "";
	}

	public static ConnectorOrderingSyncOption jobnetConnectorOrderingSyncOption(Unit unit) {
		if (jobnetConnectorOrdering(unit)) {
			final Param p = findParamOne(unit, "ncs");
			return p != null && p.getValue().equals("y") ? ConnectorOrderingSyncOption.SYNC : ConnectorOrderingSyncOption.ASYNC;
		} else {
			return null;
		}
	}
	
	// For Connectables
	public static boolean jobnetConnectorOrderingExchangeOption(Unit unit) {
		final Param r = findParamOne(unit, "ncex");
		if (r != null && r.getValue().equals("y")) {
			return true;
		} else {
			return false;
		}
	}

	public static String jobnetConnectorHostName(Unit unit) {
		final Param r = findParamOne(unit, "nchn");
		if (r != null) {
			return r.getValue();
		} else {
			return "";
		}
	}

	public static String jobnetConnectorServiceName(Unit unit) {
		final Param r = findParamOne(unit, "ncsv");
		if (r != null) {
			return r.getValue();
		} else {
			return "";
		}
	}

	// For Executables
	/**
	 * 対象ユニットの保留属性設定タイプを返す.
	 * @return 保留属性設定タイプ
	 */
	public static HoldType holdType(Unit unit) {
		final Param p = findParamOne(unit, "ha");
		return p == null ? HoldType.NO
				: HoldType.searchByAbbr(p.getValues().get(0)
						.getStringValue());
	}

	public static int fixedDuration(Unit unit) {
		final Param p = findParamOne(unit, "fd");
		return p == null ? -1 : Integer.parseInt(p.getValues().get(0).getStringValue());
	}
	/**
	 * 対象ユニットの実行ホスト名を返す.
	 * @return 実行ホスト名
	 */

	public static String executionHostName(Unit unit) {
		final Param p = findParamOne(unit, "ex");
		return p == null ? null : p.getValues().get(0).getStringValue();
	}

	public static ExecutionUserType executionUserType(Unit unit) {
		final Param r = findParamOne(unit, "eu");
		if (r != null && r.getValue().equals("def")) {
			return ExecutionUserType.DEFINITION_USER;
		} else {
			return ExecutionUserType.ENTRY_USER;
		}
	}

	public static int executionTimeOut(Unit unit) {
		final Param p = findParamOne(unit, "etm");
		if (p != null) {
			final String s = p.getValue();
			return s.isEmpty() ? -1 : Integer.parseInt(s);
		} else {
			return -1;
		}
	}
	
	// For Jobnets
	/**
	 * 引数で指定されたユニットのサブユニットの集合から関連線で結ばれたユニットのペアのリストを返す. このメソッドが返す{@link Arrow}
	 * はJP1定義にあらかじめ規定された概念ではありません。 JP1定義解析の便宜のため、このライブラリにおいて独自に規定しているものです。
	 * JP1ユニット定義パラメータの1つである"ar"の内容をもとに生成されます。
	 * 
	 * @return 関連線で結ばれたユニットのペアのリスト
	 */
	public static List<Arrow> arrows(Unit unit) {
		final List<Arrow> result = new ArrayList<Arrow>();
		for (final Param p : unit.getParams()) {
			if (p.getName().equals("ar")) {
				final List<ParamValue> pvs = p.getValues();
				if (pvs.size() > 0) {
					if (pvs.get(0).is(ParamValueType.TUPLOID)) {
						final Tuple t = pvs.get(0).getTuploidValue();

						result.add(new Arrow(
								unit.getSubUnit(t.get("f")),
								unit.getSubUnit(t.get("t")),
								t.size() == 3 ? UnitConnectionType.searchByAbbr(t.get(2)) : UnitConnectionType.SEQUENTIAL));
					}
				}
			}
		}
		return result;
	}

	// For Judgments
	/**
	 * 対象ユニットの判定条件タイプを返す.
	 * @param unit 対象ユニット
	 * @return 判定条件タイプ
	 */
	public static EvaluateConditionType evaluateConditionType(Unit unit) {
		final Param p = findParamOne(unit, "ej");
		return p == null ? EvaluateConditionType.EXIT_CODE_GT
				: EvaluateConditionType.searchByAbbr(p.getValues().get(0)
						.getStringValue());
	};
	
	/**
	 * 対象ユニットの判定終了コードを返す.
	 * @param unit 対象ユニット
	 * @return 判定終了コード（デフォルト0）
	 */
	int getEvaluableExitCode(Unit unit) {
		final Param p = findParamOne(unit, "ejc");
		return p == null ? 0 : Integer
				.parseInt(p.getValues().get(0).getStringValue());
	};
	
	/**
	 * 対象ユニットの終了判定ファイル名を返す.
	 * @param unit 対象ユニット
	 * @return 終了判定ファイル名
	 */
	String getEvaluableFileName(Unit unit) {
		final Param p = findParamOne(unit, "ejf");
		return p == null ? null : p.getValues().get(0).getStringValue();
	};
	
	/**
	 * 対象ユニットの判定対象変数名を返す.
	 * @param unit 対象ユニット
	 * @return 判定対象変数名
	 */
	String getEvaluableVariableName(Unit unit) {
		final Param p = findParamOne(unit, "ejv");
		return p == null ? null : p.getValues().get(0).getStringValue();
	};
	
	/**
	 * 対象ユニットの判定対象変数（文字列）の判定値を返す.
	 * @param unit 対象ユニット
	 * @return 判定対象変数（文字列）の判定値
	 */
	String getEvaluableVariableStringValue(Unit unit) {
		final Param p = findParamOne(unit, "ejt");
		return p == null ? null : p.getValues().get(0).getStringValue();
	};
	
	/**
	 * 対象ユニットの判定対象変数（数値）の判定値を返す.
	 * @param unit 対象ユニット
	 * @return 判定対象変数（数値）の判定値
	 */
	int getEvaluableVariableIntegerValue(Unit unit) {
		final Param p = findParamOne(unit, "eji");
		return p == null ? 0 : Integer
				.parseInt(p.getValues().get(0).getStringValue());
	};
	
	// For Mail Agents
	public static String mailProfileName(Unit unit) {
		final Param p = findParamOne(unit, "mlprf");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}

	public static List<MailAddress> mailAddresses(Unit unit) {
		final ArrayList<MailAddress> l = new ArrayList<MailAddress>();
		final List<Param> ps = findParamAll(unit, "mladr");
		final Pattern pat = Pattern.compile("^(TO|CC|BCC):\"(.+\"$)");
		
		for (final Param p : ps) {
			final Matcher mat = pat.matcher(p.getValue());
			if (mat.matches()) {
				final String t = mat.group(1);
				final String a = mat.group(2).replaceAll("#\"", "\"").replaceAll("##", "#");
				l.add(new MailAddress(){
					@Override
					public AddressType getType() {
						return t.equals("TO") ? AddressType.TO : (t.equals("CC") ? AddressType.CC : AddressType.BCC);
					}
					@Override
					public String getAddress() {
						return a;
					}
				});
			}
		}
		return l;
	}

	public static String mailSubject(Unit unit) {
		final Param p = findParamOne(unit, "mlsbj");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}

	public static String mailBody(Unit unit) {
		final Param p = findParamOne(unit, "mltxt");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}

	public static String attachmentFileListPath(Unit unit) {
		final Param p = findParamOne(unit, "mlafl");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	
	// For Mail Sends
	public static String mailBodyFilePath(Unit unit) {
		final Param p = findParamOne(unit, "mlftx");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String mailAttachmentFilePath(Unit unit) {
		final Param p = findParamOne(unit, "mlatf");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}

	// For Unix/Pc Job
	/**
	 * 対象ユニットの警告終了閾値を返す.
	 * @return 警告終了閾値（0〜2,147,483,647）
	 */
	public int warningThreshold(Unit unit) {
		final Param p = findParamOne(unit, "wth");
		return p == null ? -1 : Integer.parseInt(p.getValues().get(0)
				.getStringValue());
	}
	/**
	 * 対象ユニットの異常終了閾値を返す.
	 * @return 異常終了閾値（0〜2,147,483,647。デフォルト値は0）
	 */
	public static int errorThreshold(Unit unit) {
		final Param p = findParamOne(unit, "tho");
		return  p == null ? 0 : Integer
				.parseInt(p.getValues().get(0).getStringValue());
	}
	/**
	 * 対象ユニットの終了判定種別を返す.
	 * @return 終了判定種別
	 */
	public static ResultJudgmentType resultJudgmentType(Unit unit) {
		final Param p = findParamOne(unit, "jd");
		return p == null ? ResultJudgmentType.DEPENDS_ON_EXIT_CODE
				: ResultJudgmentType.searchByAbbr(p.getValues().get(0)
						.getStringValue());
	}
	/**
	 * 対象ユニットの実行ユーザ名を返す.
	 * @return 実行ユーザ名
	 */
	public static String executionUserName(Unit unit) {
		final Param p = findParamOne(unit, "un");
		return p == null ? null : p.getValues().get(0).getStringValue();
	}
	/**
	 * 対象ユニットのスクリプトファイル名（UNIXジョブ）もしくは実行ファイル名（PCジョブ）を返す.
	 * @return スクリプトファイル名（UNIXジョブ）もしくは実行ファイル名（PCジョブ）
	 */
	public static String scriptFilePath(Unit unit) {
		final Param p = findParamOne(unit, "un");
		return p == null ? null : p.getValues().get(0).getStringValue();
	}
	/**
	 * 対象ユニットの実行ファイルに対するパラメータの設定値を返す.
	 * @return 実行ファイルに対するパラメータ
	 */
	public static String parameter(Unit unit) {
		final Param p = findParamOne(unit, "prm");
		return p == null ? null : p.getValues().get(0).getStringValue();
	}
	public static String transportSourceFilePath1(Unit unit) {
		final Param p = findParamOne(unit, "ts1");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String transportDestinationFilePath1(Unit unit) {
		final Param p = findParamOne(unit, "td1");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String transportSourceFilePath2(Unit unit) {
		final Param p = findParamOne(unit, "ts2");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String transportDestinationFilePath2(Unit unit) {
		final Param p = findParamOne(unit, "td2");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String transportSourceFilePath3(Unit unit) {
		final Param p = findParamOne(unit, "ts3");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String transportDestinationFilePath3(Unit unit) {
		final Param p = findParamOne(unit, "td3");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String transportSourceFilePath4(Unit unit) {
		final Param p = findParamOne(unit, "ts4");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String transportDestinationFilePath4(Unit unit) {
		final Param p = findParamOne(unit, "td4");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String commandText(Unit unit) {
		final Param p = findParamOne(unit, "te");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String workPath(Unit unit) {
		final Param p = findParamOne(unit, "wkp");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String environmentVariableFilePath(Unit unit) {
		final Param p = findParamOne(unit, "ev");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String environmentVariable(Unit unit) {
		final Param p = findParamOne(unit, "env");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String standardInputFilePath(Unit unit) {
		final Param p = findParamOne(unit, "si");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String standardOutputFilePath(Unit unit) {
		final Param p = findParamOne(unit, "so");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static String getStandardErrorFilePath(Unit unit) {
		final Param p = findParamOne(unit, "se");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static WriteOption standardOutputWriteOption(Unit unit) {
		final Param r = findParamOne(unit, "soa");
		if (r != null && r.getValue().equals("add")) {
			return WriteOption.ADD;
		} else {
			return WriteOption.NEW;
		}
	}
	public static WriteOption standardErrorWriteOption(Unit unit) {
		final Param r = findParamOne(unit, "sea");
		if (r != null && r.getValue().equals("add")) {
			return WriteOption.ADD;
		} else {
			return WriteOption.NEW;
		}
	}
	public static String resultJudgementFilePath(Unit unit) {
		final Param p = findParamOne(unit, "jdf");
		if (p != null) {
			return p.getValue();
		} else {
			return "";
		}
	}
	public static DeleteOption transportDestinationFileDeleteOption1(Unit unit) {
		final Param p = findParamOne(unit, "top1");
		if (p != null) {
			final String s = p.getValue();
			if (s.equals("sav")) {
				return DeleteOption.SAVE;
			} else if (s.equals("del")) {
				return DeleteOption.DELETE;
			}
		}
		final String ts = transportSourceFilePath1(unit);
		final String td = transportDestinationFilePath1(unit);
		if (!ts.isEmpty() && !td.isEmpty()) {
			return DeleteOption.SAVE;
		} else if (!ts.isEmpty() && td.isEmpty()) {
			return DeleteOption.DELETE;
		} else {
			return null;
		}
	}
	public static DeleteOption transportDestinationFileDeleteOption2(Unit unit) {
		final Param p = findParamOne(unit, "top2");
		if (p != null) {
			final String s = p.getValue();
			if (s.equals("sav")) {
				return DeleteOption.SAVE;
			} else if (s.equals("del")) {
				return DeleteOption.DELETE;
			}
		}
		final String ts = transportSourceFilePath2(unit);
		final String td = transportDestinationFilePath2(unit);
		if (!ts.isEmpty() && !td.isEmpty()) {
			return DeleteOption.SAVE;
		} else if (!ts.isEmpty() && td.isEmpty()) {
			return DeleteOption.DELETE;
		} else {
			return null;
		}
	}
	public static DeleteOption transportDestinationFileDeleteOption3(Unit unit) {
		final Param p = findParamOne(unit, "top3");
		if (p != null) {
			final String s = p.getValue();
			if (s.equals("sav")) {
				return DeleteOption.SAVE;
			} else if (s.equals("del")) {
				return DeleteOption.DELETE;
			}
		}
		final String ts = transportSourceFilePath3(unit);
		final String td = transportDestinationFilePath3(unit);
		if (!ts.isEmpty() && !td.isEmpty()) {
			return DeleteOption.SAVE;
		} else if (!ts.isEmpty() && td.isEmpty()) {
			return DeleteOption.DELETE;
		} else {
			return null;
		}
	}
	public static DeleteOption transportDestinationFileDeleteOption4(Unit unit) {
		final Param p = findParamOne(unit, "top4");
		if (p != null) {
			final String s = p.getValue();
			if (s.equals("sav")) {
				return DeleteOption.SAVE;
			} else if (s.equals("del")) {
				return DeleteOption.DELETE;
			}
		}
		final String ts = transportSourceFilePath4(unit);
		final String td = transportDestinationFilePath4(unit);
		if (!ts.isEmpty() && !td.isEmpty()) {
			return DeleteOption.SAVE;
		} else if (!ts.isEmpty() && td.isEmpty()) {
			return DeleteOption.DELETE;
		} else {
			return null;
		}
	}

}
