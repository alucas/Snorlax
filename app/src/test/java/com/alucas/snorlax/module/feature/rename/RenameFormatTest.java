/*
 * Copyright (c) 2016. Pedro Diaz <igoticecream@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alucas.snorlax.module.feature.rename;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import com.alucas.snorlax.module.pokemon.Pokemon;
import com.alucas.snorlax.module.pokemon.PokemonFactory;
import com.alucas.snorlax.module.util.Log;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import POGOProtos.Enums.PokemonTypeOuterClass.PokemonType;
import POGOProtos.Settings.Master.MoveSettingsOuterClass.MoveSettings;

import static POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import static POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
	Pokemon.class,
	PokemonData.class,
	PokemonFactory.class,
	MoveSettings.class,
	RenamePreferences.class,
	Log.class
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RenameFormatTest {

	private static final String POKEMON_NAME = "Snorlax";
	private static final String POKEMON_NICKNAME = "Snacks";
	private static final float POKEMON_LEVEL = 8.5f;
	private static final int POKEMON_ATTACK = 1;
	private static final int POKEMON_DEFENSE = 1;
	private static final int POKEMON_STAMINA = 1;
	// A/D/S -> 0.0666666666666667

	@Mock
	private PokemonFactory mPokemonFactory;
	@Mock
	private Pokemon mPokemon;
	@Mock
	private RenamePreferences mRenamePreferences;
	@Mock
	private PokemonData mProto;
	@Mock
	private MoveSettings mPokemonSettings;

	@InjectMocks
	private RenameFormat mSut;

	private String mExpected;

	@Before
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public void setUp() throws Exception {
		// Given
		PowerMockito.mockStatic(Log.class);

		Mockito.doReturn(mPokemon).when(mPokemonFactory).with(mProto);

		Mockito.doReturn(5f).when(mPokemonSettings).getPower();
		Mockito.doReturn(PokemonType.POKEMON_TYPE_PSYCHIC).when(mPokemonSettings).getPokemonType();
		Mockito.doReturn(PokemonMove.ZEN_HEADBUTT_FAST).when(mPokemonSettings).getMovementId();
		Mockito.doCallRealMethod().when(mPokemonSettings).toString();

		Mockito.doReturn(POKEMON_NAME).when(mPokemon).getName();
		Mockito.doReturn(POKEMON_NICKNAME).when(mPokemon).getNickname();
		Mockito.doReturn(POKEMON_LEVEL).when(mPokemon).getLevel();
		Mockito.doReturn(POKEMON_ATTACK).when(mPokemon).getIVAttack();
		Mockito.doReturn(POKEMON_DEFENSE).when(mPokemon).getIVDefense();
		Mockito.doReturn(POKEMON_STAMINA).when(mPokemon).getIVStamina();
		Mockito.doReturn(50).when(mPokemon).getCp();
		Mockito.doReturn(100).when(mPokemon).getLastEvolutionCp();
		Mockito.doCallRealMethod().when(mPokemon).getIv();

		Mockito.doReturn(mPokemonSettings).when(mPokemon).getMoveFast();
		Mockito.doReturn(mPokemonSettings).when(mPokemon).getMoveCharge();

		Mockito.doReturn(PokemonType.POKEMON_TYPE_NORMAL).when(mPokemon).getType1();
		Mockito.doReturn(PokemonType.POKEMON_TYPE_NONE).when(mPokemon).getType2();
	}

	@After
	public void tearDown() throws Exception {
		// When
		final String formatted = mSut.format(mProto, new HashMap<>());

		// Then
		Mockito.verify(mPokemonFactory).with(mProto);
		Mockito.verify(mRenamePreferences).getFormat();
		Mockito.verifyNoMoreInteractions(mPokemonFactory, mRenamePreferences);

		MatcherAssert.assertThat(formatted, Matchers.is(mExpected));
	}

	//region Command processing
	@Test
	public void testCommandCompleteFormat() throws Exception {
		mExpected = POKEMON_NAME;
		setRenameFormat("%NAME%");
	}

	private void setRenameFormat(String format) {
		Mockito
			.doReturn(format)
			.when(mRenamePreferences)
			.getFormat();
	}

	@Test
	public void testCommandIncompleteFormat() throws Exception {
		mExpected = "%NAME";
		setRenameFormat("%NAME");
	}

	@Test
	public void testCommandFormatWithPlainText() throws Exception {
		setRenameFormat("Plain Text %NAME%");
		mExpected = "Plain Text " + POKEMON_NAME;
	}

	@Test
	public void testCommandFormatWithSpaces() throws Exception {
		setRenameFormat("%NAME %NAME%");
		mExpected = "%NAME " + POKEMON_NAME;
	}

	@Test
	public void testNotCommand() throws Exception {
		mExpected = "%SNORLAX%";
		setRenameFormat("%SNORLAX%");
	}

	@Test
	public void testSomeCommandCombined() throws Exception {
		mExpected = POKEMON_NAME.substring(0, 3) + " 00" + DecimalFormat.getInstance().format(6.7) + " 1/1/1 " + DecimalFormat.getInstance().format(8.5);
		setRenameFormat("%NAME.3% %IVP.1% %ATT%/%DEF%/%STA% %LVL%");
	}

	@Test
	public void testSomeOthersCommandCombined() throws Exception {
		mExpected = POKEMON_NAME + " " + DecimalFormat.getInstance().format(6.7) + "%,1/1/1," + DecimalFormat.getInstance().format(8.5);
		setRenameFormat("%NAME% %IV%%,%ATT%/%DEF%/%STA%,%LVL%");
	}

	@Test
	public void testSomeOthers2CommandCombined() throws Exception {
		mExpected = DecimalFormat.getInstance().format(6.7) + " 1/1/1 " + DecimalFormat.getInstance().format(8.5) + " (" + NumberFormat.getInstance().format(100) + ")";
		setRenameFormat("%IV% %ATT%/%DEF%/%STA% %LVL% (%CPL%)");
	}

	@Test
	public void testStartDelimiter2() throws Exception {
		mExpected = POKEMON_NAME;
		setRenameFormat("℅NAME%");
	}

	@Test
	public void testEndDelimiter2() throws Exception {
		mExpected = POKEMON_NAME;
		setRenameFormat("%NAME℅");
	}

	@Test
	public void testDelimiter2() throws Exception {
		mExpected = POKEMON_NAME;
		setRenameFormat("℅NAME℅");
	}
	//endregion

	//region Name
	@Test
	public void testNameTruncateBelowLength() throws Exception {
		mExpected = POKEMON_NAME.substring(0, 3);
		setRenameFormat("%NAME.3%");
	}
	//endregion

	//region Nickname
	@Test
	public void testNickname() throws Exception {
		mExpected = POKEMON_NICKNAME;
		setRenameFormat("%NICK%");
	}

	@Test
	public void testNameTruncateExactLength() throws Exception {
		mExpected = POKEMON_NAME;
		setRenameFormat("%NAME.7%");
	}

	@Test
	public void testNameTruncateAboveLength() throws Exception {
		mExpected = POKEMON_NAME;
		setRenameFormat("%NAME.30%");
	}

	@Test
	public void testNameTruncateIncompleteFormat() throws Exception {
		mExpected = "%NAME.%";
		setRenameFormat("%NAME.%");
	}

	@Test
	public void testNameTruncateWrongFormat() throws Exception {
		mExpected = "%NAME.1a%";
		setRenameFormat("%NAME.1a%");
	}
	//endregion

	//region Level
	@Test
	public void testLevel() throws Exception {
		mExpected = DecimalFormat.getInstance().format(8.5);
		setRenameFormat("%LVL%");
	}

	@Test
	public void testLevelNoDecimal() throws Exception {
		mExpected = "9";
		setRenameFormat("%LVL.0%");
	}

	@Test
	public void testLevelOneDecimal() throws Exception {
		mExpected = DecimalFormat.getInstance().format(8.5);
		setRenameFormat("%LVL.1%");
	}

	@Test
	public void testLevelMoreDecimal() throws Exception {
		mExpected = DecimalFormat.getInstance().format(8.5) + "00";
		setRenameFormat("%LVL.3%");
	}

	@Test
	public void testLevelWrongDecimal() throws Exception {
		mExpected = "%LVL.A%";
		setRenameFormat("%LVL.A%");
	}

	@Test
	public void testLevelWithPadding() throws Exception {
		mExpected = "0" + DecimalFormat.getInstance().format(8.5);
		setRenameFormat("%LVLP%");
	}

	@Test
	public void testLevelWithPaddingNoDecimal() throws Exception {
		mExpected = "09";
		setRenameFormat("%LVLP.0%");
	}

	@Test
	public void testLevelWithPaddingOneDecimal() throws Exception {
		mExpected = "0" + DecimalFormat.getInstance().format(8.5);
		setRenameFormat("%LVLP.1%");
	}

	@Test
	public void testLevelWithPaddingMoreDecimal() throws Exception {
		mExpected = "0" + DecimalFormat.getInstance().format(8.5) + "00";
		setRenameFormat("%LVLP.3%");
	}

	@Test
	public void testLevelWithPaddingWrongDecimal() throws Exception {
		mExpected = "%LVLP.A%";
		setRenameFormat("%LVLP.A%");
	}
	//endregion

	//region Iv
	@Test
	public void testIv() throws Exception {
		mExpected = DecimalFormat.getInstance().format(6.7);
		setRenameFormat("%IV%");
	}

	@Test
	public void testIvNoDecimal() throws Exception {
		mExpected = "7";
		setRenameFormat("%IV.0%");
	}

	@Test
	public void testIvOneDecimal() throws Exception {
		mExpected = DecimalFormat.getInstance().format(6.7);
		setRenameFormat("%IV.1%");
	}

	@Test
	public void testIvMoreDecimal() throws Exception {
		mExpected = DecimalFormat.getInstance().format(6.667);
		setRenameFormat("%IV.3%");
	}

	@Test
	public void testIvWrongDecimal() throws Exception {
		mExpected = "%IV.A%";
		setRenameFormat("%IV.A%");
	}

	@Test
	public void testIvWithPadding() throws Exception {
		mExpected = "00" + DecimalFormat.getInstance().format(6.7);
		setRenameFormat("%IVP%");
	}

	@Test
	public void testIvWithPaddingNoDecimal() throws Exception {
		mExpected = "007";
		setRenameFormat("%IVP.0%");
	}

	@Test
	public void testIvWithPaddingOneDecimal() throws Exception {
		mExpected = "00" + DecimalFormat.getInstance().format(6.7);
		setRenameFormat("%IVP.1%");
	}

	@Test
	public void testIvWithPaddingMoreDecimal() throws Exception {
		mExpected = "00" + DecimalFormat.getInstance().format(6.667);
		setRenameFormat("%IVP.3%");
	}

	@Test
	public void testIvWithPaddingWrongDecimal() throws Exception {
		mExpected = "%IVP.A%";
		setRenameFormat("%IVP.A%");
	}
	//endregion

	//region Attack
	@Test
	public void testAttack() throws Exception {
		mExpected = "1";
		setRenameFormat("%ATT%");
	}

	@Test
	public void testAttackUnknown() throws Exception {
		mExpected = "%ATTW%";
		setRenameFormat("%ATTW%");
	}

	@Test
	public void testAttackTwoDigits() throws Exception {
		Mockito.doReturn(10).when(mPokemon).getIVAttack();

		mExpected = "10";
		setRenameFormat("%ATT%");
	}

	@Test
	public void testAttackWithPadding() throws Exception {
		mExpected = "01";
		setRenameFormat("%ATTP%");
	}

	@Test
	public void testAttackHex() throws Exception {
		Mockito.doReturn(15).when(mPokemon).getIVAttack();

		mExpected = "F";
		setRenameFormat("%ATTH%");
	}
	//endregion

	//region Defense
	@Test
	public void testDefense() throws Exception {
		mExpected = "1";
		setRenameFormat("%DEF%");
	}

	@Test
	public void testDefenseUnknown() throws Exception {
		mExpected = "%DEFW%";
		setRenameFormat("%DEFW%");
	}

	@Test
	public void testDefenseTwoDigits() throws Exception {
		Mockito.doReturn(10).when(mPokemon).getIVDefense();

		mExpected = "10";
		setRenameFormat("%DEF%");
	}

	@Test
	public void testDefenseWithPadding() throws Exception {
		mExpected = "01";
		setRenameFormat("%DEFP%");
	}

	@Test
	public void testDefenseHex() throws Exception {
		Mockito.doReturn(15).when(mPokemon).getIVDefense();

		mExpected = "F";
		setRenameFormat("%DEFH%");
	}
	//endregion

	//region Stamina
	@Test
	public void testStamina() throws Exception {
		mExpected = "1";
		setRenameFormat("%STA%");
	}

	@Test
	public void testStaminaUnknown() throws Exception {
		mExpected = "%STAW%";
		setRenameFormat("%STAW%");
	}

	@Test
	public void testStaminaTwoDigits() throws Exception {
		Mockito.doReturn(10).when(mPokemon).getIVStamina();

		mExpected = "10";
		setRenameFormat("%STA%");
	}

	@Test
	public void testStaminaWithPadding() throws Exception {
		mExpected = "01";
		setRenameFormat("%STAP%");
	}

	@Test
	public void testStaminaHex() throws Exception {
		Mockito.doReturn(15).when(mPokemon).getIVStamina();

		mExpected = "F";
		setRenameFormat("%STAH%");
	}
	//endregion

	//region Move
	@Test
	public void testMoveFast() throws Exception {
		mExpected = "Zen Headbutt";
		setRenameFormat("%MV1%");
	}

	@Test
	public void testMoveCharge() throws Exception {
		mExpected = "Zen Headbutt";
		setRenameFormat("%MV2%");
	}

	@Test
	public void testMoveFastNull() throws Exception {
		Mockito.doReturn(null).when(mPokemon).getMoveFast();

		mExpected = "%MV1%";
		setRenameFormat("%MV1%");
	}

	@Test
	public void testMoveUnknown() throws Exception {
		mExpected = "%MV3%";
		setRenameFormat("%MV3%");
	}

	@Test
	public void testMoveChargeNull() throws Exception {
		Mockito.doReturn(null).when(mPokemon).getMoveCharge();

		mExpected = "%MV2%";
		setRenameFormat("%MV2%");
	}

	@Test
	public void testMoveTruncateBelowLength() throws Exception {
		mExpected = "Zen Headbutt".substring(0, 3);
		setRenameFormat("%MV1.3%");
	}

	@Test
	public void testMoveTruncateExactLength() throws Exception {
		mExpected = "Zen Headbutt";
		setRenameFormat("%MV1.12%");
	}

	@Test
	public void testMoveTruncateAboveLength() throws Exception {
		mExpected = "Zen Headbutt";
		setRenameFormat("%MV1.30%");
	}

	@Test
	public void testMoveTruncateIncompleteFormat() throws Exception {
		mExpected = "%MV1.%";
		setRenameFormat("%MV1.%");
	}

	@Test
	public void testMoveTruncateWrongFormat() throws Exception {
		mExpected = "%MV1.1a%";
		setRenameFormat("%MV1.1a%");
	}
	//endregion

	//region Move type
	@Test
	public void testMoveTypeFast() throws Exception {
		mExpected = "Psychic";
		setRenameFormat("%MVT1%");
	}

	@Test
	public void testMoveTypeCharge() throws Exception {
		mExpected = "Psychic";
		setRenameFormat("%MVT2%");
	}

	@Test
	public void testMoveTypeUnknown() throws Exception {
		mExpected = "%MVT3%";
		setRenameFormat("%MVT3%");
	}

	@Test
	public void testMoveTypeFastNull() throws Exception {
		Mockito.doReturn(null).when(mPokemon).getMoveFast();

		mExpected = "%MVT1%";
		setRenameFormat("%MVT1%");
	}

	@Test
	public void testMoveTypeChargeNull() throws Exception {
		Mockito.doReturn(null).when(mPokemon).getMoveCharge();

		mExpected = "%MVT2%";
		setRenameFormat("%MVT2%");
	}

	@Test
	public void testMoveTypeTruncateBelowLength() throws Exception {
		mExpected = "Psychic".substring(0, 3);
		setRenameFormat("%MVT1.3%");
	}

	@Test
	public void testMoveTypeTruncateExactLength() throws Exception {
		mExpected = "Psychic";
		setRenameFormat("%MVT1.7%");
	}

	@Test
	public void testMoveTypeTruncateAboveLength() throws Exception {
		mExpected = "Psychic";
		setRenameFormat("%MVT1.30%");
	}

	@Test
	public void testMoveTypeTruncateLength2() throws Exception {
		mExpected = "Py";
		setRenameFormat("%MVT1.2%");
	}

	@Test
	public void testMoveTypeTruncateIncompleteFormat() throws Exception {
		mExpected = "%MVT1.%";
		setRenameFormat("%MVT1.%");
	}

	@Test
	public void testMoveTypeTruncateWrongFormat() throws Exception {
		mExpected = "%MVT1.1a%";
		setRenameFormat("%MVT1.1a%");
	}
	//endregion

	//region Move power
	@Test
	public void testMovePowerFast() throws Exception {
		mExpected = "5";
		setRenameFormat("%MVP1%");
	}

	@Test
	public void testMovePowerFastWithPadding() throws Exception {
		mExpected = "005";
		setRenameFormat("%MVP1P%");
	}

	@Test
	public void testMovePowerCharge() throws Exception {
		mExpected = "5";
		setRenameFormat("%MVP2%");
	}

	@Test
	public void testMovePowerChargeWithPadding() throws Exception {
		mExpected = "005";
		setRenameFormat("%MVP2P%");
	}

	@Test
	public void testMovePowerUnknown() throws Exception {
		mExpected = "%MVP1W%";
		setRenameFormat("%MVP1W%");
	}

	@Test
	public void testMovePowerFastNull() throws Exception {
		Mockito.doReturn(null).when(mPokemon).getMoveFast();

		mExpected = "%MVP1%";
		setRenameFormat("%MVP1%");
	}

	@Test
	public void testMovePowerChargeNull() throws Exception {
		Mockito.doReturn(null).when(mPokemon).getMoveCharge();

		mExpected = "%MVP2%";
		setRenameFormat("%MVP2%");
	}
	//endregion

	//region Type
	@Test
	public void testType1() throws Exception {
		mExpected = "Normal";
		setRenameFormat("%TYP1%");
	}

	@Test
	public void testType2() throws Exception {
		mExpected = "None";
		setRenameFormat("%TYP2%");
	}

	@Test
	public void testTypeUnknown() throws Exception {
		mExpected = "%TYP3%";
		setRenameFormat("%TYP3%");
	}

	@Test
	public void testTypeTruncateBelowLength() throws Exception {
		mExpected = "Normal".substring(0, 3);
		setRenameFormat("%TYP1.3%");
	}

	@Test
	public void testTypeTruncateExactLength() throws Exception {
		mExpected = "Normal";
		setRenameFormat("%TYP1.6%");
	}

	@Test
	public void testTypeTruncateAboveLength() throws Exception {
		mExpected = "Normal";
		setRenameFormat("%TYP1.30%");
	}

	@Test
	public void testTypeTruncateLength2() throws Exception {
		mExpected = "Nr";
		setRenameFormat("%TYP1.2%");
	}

	@Test
	public void testTypeTruncateIncompleteFormat() throws Exception {
		mExpected = "%TYP1.%";
		setRenameFormat("%TYP1.%");
	}

	@Test
	public void testTypeTruncateWrongFormat() throws Exception {
		mExpected = "%TYP1.1a%";
		setRenameFormat("%TYP1.1a%");
	}
	//endregion

	//region Cp
	@Test
	public void testCP() throws Exception {
		mExpected = "50";
		setRenameFormat("%CP%");
	}

	@Test
	public void testCPLast() throws Exception {
		mExpected = "100";
		setRenameFormat("%CPL%");
	}
	//endregion
}
