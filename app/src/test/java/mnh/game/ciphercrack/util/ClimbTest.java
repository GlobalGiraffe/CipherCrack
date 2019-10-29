package mnh.game.ciphercrack.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Properties;

import mnh.game.ciphercrack.cipher.Vigenere;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ClimbTest {

    private static final String defaultAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Test
    public void testClimb() {
        // Cipher Challenge 2005 7A
        String text = "TYKZK LASFY YDKYC DERKF LRJKY AIFEU HSZER LRVJR\n" +
                "PNXKM ZAPKF LLVRQ AWVFZ AAZEC KPYFR VSFWR OEEVU\n" +
                "JIGYC YMRTF PNVRL KBVCG LVVZR VREFR HFLCJ ZEKFD\n" +
                "DIIZL NDZRE YADJR VGVKF LRNZR OTYVQ LTKZL NSWFP\n" +
                "AHVEC ETDFL AHRJD HRRJG RNFNM BRGIC ZEETC DAJEM\n" +
                "ADVKC JTVUR OEIVU HSEFQ PGEFD SIWVY UDZXS LSJFS\n" +
                "YDZMC YSZFL DOIBC KTYVJ VWMZQ PBZCG AYFER OEZTC\n" +
                "OECGC KTYVK HCYZL LIJKS KIVUU HSIVK HRBRZ SYLEQ\n" +
                "VPYZQ AITRR LDREB IOIVJ PTKCC YEJVK ILREA LTFKF\n" +
                "LTVEU OEVCP VTFIK HCYZL LOLIA VNKRA ASYRB SEULQ\n" +
                "AOVON LCKZR TAPSC AHRKY ZANVY AHVIQ AAKZM UMFUC\n" +
                "SIKYY ZBVVL REGKQ PMGCC AHVPK HYRCQ VHRMC IEVEU\n" +
                "VRIZC KTYRR PTNFS SDWRJ SIEKM LNVDW OAEUQ HNUKF\n" +
                "LRVWM YEYRT LIJJS LDFEJ FTYZQ WRFKM AYGVL VNVKF\n" +
                "LLVJQ AHVPQ LEDTM UVZEA LDFWG ASJVA BRZKW AIDVU\n" +
                "PLCKC SLZNG SLXZT LAWLJ SEIIC WOIKM UTYVQ ARLTR\n" +
                "BRVFD AHVDY JHZEC PTJFN LRRKG VNREB PTJTS YRVER\n" +
                "ZEKKG UGJZL TYEVV AMVJQ HGVSW AHVNY FIWFS UDKYC\n" +
                "LNTCM ZEUGJ HIEKC ETEVV ATFKF LMRTF PNVRL KIKCM\n" +
                "VKVUY ZTYFS NHZKU HSRSM BTKFZ LSVER PTDRW IENFP\n" +
                "AHCZQ AEEZL NOLKD VRKYC UEOKU LAKYC YSKRR POEKP\n" +
                "HNJDG ZSZFL PNKYC TERER PMVZU PLCRR AATBR OECRR\n" +
                "LSKJM CIVKK LSJRE LTYVW ZEVDR VHRMC ZCRCC KUGKF\n" +
                "LSVTS YIKPY NAZER OOLXF PDFER AHZEI AHZJM UELJC\n" +
                "ZTYVP VTFIK HCYZL LSFZR ZHFLJ KNKSC AOFJR LEG\n";
        text = text.replaceAll("\\W+","");
        Properties props = new Properties();
        props.setProperty(Climb.CLIMB_START_KEYWORD, "AAAAA");
        props.setProperty(Climb.CLIMB_ALPHABET, defaultAlphabet);
        props.setProperty(Climb.CLIMB_CRIBS, "contacts,again");

        boolean success = Climb.doClimb(text, new Vigenere(null), props);
        String bestKeyword = props.getProperty(Climb.CLIMB_BEST_KEYWORD);
        String bestDecode = props.getProperty(Climb.CLIMB_BEST_DECODE);
        String activity = props.getProperty(Climb.CLIMB_ACTIVITY);

        assertTrue("Climb Check Success", success);
        assertEquals("Climb Cracked keyword", "HARRY", bestKeyword);
        assertNotNull("Climb Cracked activity", activity);

        String expected = "mytimeaboardtheweatherstationwasinterestingtosaytheleastweobtainedphotosofthenewciphermachineandbelieveitornotafullsetofwiringdiagramstogetherwiththesettingsforthenextmonthasfarasiknowourpresencewasnotdetectedtherewasnosignoflifeandiguessourdiversionworkedthelowvisibilityontheicehelpedthemachineistudiedwasremarkablyunsophisticatedandborelittleresemblancetothetenwheelrotormachineourcontactshadledustoexpectitmaybethatasaweatherstationmodelithasbeenkeptsimpletheymayalsohavebeenworriedthatitwouldfallintoenemyhandsandthereforehaveissuedonlythisprototypenonethelesstheyseemconvincedofitssecuritytimewilltelliwillgiveafullerreportonthestructureofthemachineitsoperationanditscurrentsettingsinmynextmessagebythewayifoundtheenclosedplaintextnexttothemachineanditlookedasthoughitwasabouttobesentitmaybeworthlisteningoutforthenextweatherstationtransmissioninthemeantimeiwillattackthelatestsovietmessagetheyseemtohavescaledupthesecurityagainthoughidontthinkthisoneusestherotormachinesoitshouldntbetoosteep";
        assertEquals("Climb Cracked text", expected, bestDecode.toLowerCase());
    }
}
