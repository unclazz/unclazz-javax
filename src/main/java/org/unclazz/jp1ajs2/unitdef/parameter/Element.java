package org.unclazz.jp1ajs2.unitdef.parameter;

/**
 * ユニット定義パラメータel（要素ユニット）を表わすオブジェクト.
 * ジョブネット内のユニットをJP1/AJS2 - Viewのウィンドウに表示する際の位置情報を保持する。
 * 水平位置・垂直位置として指定可能な値に関して公式リファレンスの記載に不可解なところがあるため
 * （詳細については{@link #getHPixel()}や{@link #getVPixel()}のドキュメントを参照）、
 * 実装コードで厳密なバリデーションを行うべきではない。
 */
public interface Element {
	/**
	 * elパラメータの対象ユニットのユニット名を取得する.
	 * @return ユニット名
	 */
	String getUnitName();
	/**
	 * elパラメータの対象ユニットのユニット種別を取得する.
	 * @return ユニット種別
	 */
	UnitType getUnitType();
	/**
	 * ユニットアイコンの水平位置のピクセル値を取得する.
	 * 公式リファレンスにはピクセル値の取りうる値は0〜16,000となっているが、
	 * 同時にピクセル値は「H=80＋160x（x：0～横アイコン数－1）」という方程式も満たす必要があるため、
	 * 通常その値は80〜15,920ということになる（X座標で0〜99）。
	 * 例外はジョブグループおよび起動条件ユニットでありこれらのユニットでは0となる（そのように仮定される）。
	 * @return 水平位置のピクセル値
	 */
	int getHPixel();
	/**
	 * ユニットアイコンの垂直位置のピクセル値を取得する.
	 * 公式リファレンスにはピクセル値の取りうる値は0〜10,000となっているが、
	 * 同時にピクセル値は「V=48＋96y（y：0～縦アイコン数－1）」という方程式も満たす必要があるため、
	 * 通常その値は48〜9,936ということになる（Y座標で0〜103）。
	 * 例外はジョブグループおよび起動条件ユニットでありこれらのユニットでは0となる（そのように仮定される）。
	 * @return 垂直位置のピクセル値
	 */
	int getVPixel();
	/**
	 * ユニットアイコンの水平位置を取得する.
	 * このelパラメータが指すユニットがジョブグループや起動条件ユニットである場合、
	 * @return ユニットアイコンの水平位置（0から始まる）
	 */
	int getXCoord();
	/**
	 * ユニットアイコンの垂直位置を取得する.
	 * このelパラメータが指すユニットがジョブグループや起動条件ユニットである場合、
	 * @return ユニットアイコンの垂直位置（0から始まる）
	 */
	int getYCoord();
}