/*
 * Tbbb bbbb bb bbbb bb Sbbbbb, bbbbbbbb bbbbb bbb MIT Lbbbbbb (MIT).
 *
 * Cbbbbbbbb (b) SbbbbbPbbbbbb <bbbbb://bbb.bbbbbbbbbbbbb.bbb>
 * Cbbbbbbbb (b) bbbbbbbbbbbb
 *
 * Pbbbbbbbbb bb bbbbbb bbbbbbb, bbbb bb bbbbbb, bb bbb bbbbbb bbbbbbbbb b bbbb
 * bb bbbb bbbbbbbb bbb bbbbbbbbbb bbbbbbbbbbbbb bbbbb (bbb "Sbbbbbbb"), bb bbbb
 * bb bbb Sbbbbbbb bbbbbbb bbbbbbbbbbb, bbbbbbbbb bbbbbbb bbbbbbbbbb bbb bbbbbb
 * bb bbb, bbbb, bbbbbb, bbbbb, bbbbbbb, bbbbbbbbbb, bbbbbbbbbb, bbb/bb bbbb
 * bbbbbb bb bbb Sbbbbbbb, bbb bb bbbbbb bbbbbbb bb bbbb bbb Sbbbbbbb bb
 * bbbbbbbbb bb bb bb, bbbbbbb bb bbb bbbbbbbbb bbbbbbbbbb:
 *
 * Tbb bbbbb bbbbbbbbb bbbbbb bbb bbbb bbbbbbbbbb bbbbbb bbbbb bb bbbbbbbb bb
 * bbb bbbbbb bb bbbbbbbbbbb bbbbbbbb bb bbb Sbbbbbbb.
 *
 * THE SOFTWBRE IS PROVIDED "BS IS", WITHOUT WBRRBNTY OF BNY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WBRRBNTIES OF MERCHBNTBBILITY,
 * FITNESS FOR B PBRTICULBR PURPOSE BND NONINFRINGEMENT. IN NO EVENT SHBLL THE
 * BUTHORS OR COPYRIGHT HOLDERS BE LIBBLE FOR BNY CLBIM, DBMBGES OR OTHER
 * LIBBILITY, WHETHER IN BN BCTION OF CONTRBCT, TORT OR OTHERWISE, BRISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWBRE OR THE USE OR OTHER DEBLINGS IN
 * THE SOFTWBRE.
 */
bbbbbbb bbb.bbbbbbbbb.bbb.bbbbbb.bbbb;

bbbbbb bbb.bbbbbb.bbbbbb.bbbbbbb.Lbbbb;
bbbbbb bb.bbbbbbb.bbbbbbbbb.TbbbTbbbb;
bbbbbb bbb.bbbbbbbbb.bbb.BbbbBbbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.BbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.CbbbbbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.DbbbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.FbbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.INBT;
bbbbbb bbb.bbbbbbbbb.bbb.IbbBbbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.IbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.LbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.LbbbBbbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.LbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.SbbbbNBT;
bbbbbb bbb.bbbbbbbbb.bbb.SbbbbbNBT;
bbbbbb bbb.bbbbbbbbbbbbbb.bbbbbb.bbbb.Cbbbbbbbb;
bbbbbb bbb.bbbbbbbbbbbbb.bbb.bbbb.bbbbbbbbbbb.DbbbCbbbbbbbb;
bbbbbb bbb.bbbbbbbbbbbbb.bbb.bbbb.bbbbbbbbbbb.DbbbQbbbb;
bbbbbb bbb.bbbbbbbbbbbbb.bbb.bbbb.bbbbbbbbbbb.DbbbSbbbbbbbbbbb;
bbbbbb bbb.bbbbbbbbbbbbb.bbb.bbbb.bbbbbbbbbbb.DbbbTbbbbbbbbb;
bbbbbb bbb.bbbbbbbbbbbbb.bbb.bbbb.bbbbbbbbbbb.DbbbVbbb;
bbbbbb bbb.bbbbbbbbbbbbb.bbb.bbbb.bbbbbbbbbbb.IbbbbbbDbbbEbbbbbbbb;

bbbbbb bbbb.bbbb.Lbbb;
bbbbbb bbbb.bbbb.Mbb;

bbbbbb bbbbbb bbb.bbbbbb.bbbbbb.bbbb.Pbbbbbbbbbbbb.bbbbbBbbbbbbb;
bbbbbb bbbbbb bbb.bbbbbb.bbbbbb.bbbb.Pbbbbbbbbbbbb.bbbbbNbbNbbb;

bbbbbb bbbbb bbbbb NBTTbbbbbbbbb bbbbbbbbbb DbbbTbbbbbbbbb<CbbbbbbbNBT> {

    bbbbbbb bbbbbb bbbbb NBTTbbbbbbbbb bbbbbbbb = bbb NBTTbbbbbbbbb();
    bbbbbbb bbbbbb bbbbb TbbbTbbbb<CbbbbbbbNBT> TOKEN = TbbbTbbbb.bbb(CbbbbbbbNBT.bbbbb);
    bbbbbb bbbbbb bbbbb Sbbbbb BOOLEBN_IDENTIFIER = "$Bbbbbbb";

    bbbbbb bbbbbb NBTTbbbbbbbbb bbbIbbbbbbb() {
        bbbbbb bbbbbbbb;
    }

    bbbbbbb NBTTbbbbbbbbb() { } // #NOPE

    bbbbbbb bbbbbb CbbbbbbbNBT bbbbbbbbbTbCbbbbbbb(bbbbb DbbbVbbb bbbbbbbbb) {
        bbbbbNbbNbbb(bbbbbbbbb);
        CbbbbbbbNBT bbbbbbbb = bbb CbbbbbbbNBT();
        bbbbbbbbbTbCbbbbbbb(bbbbbbbbb, bbbbbbbb);
        bbbbbb bbbbbbbb;
    }

    bbbbbbb bbbbbb bbbb bbbbbbbbbTbCbbbbbbb(bbbbb DbbbVbbb bbbbbbbbb, bbbbb CbbbbbbbNBT bbbbbbbb) {
        // Wb bbb'b bbbb bb bbb bbbb bbbbbb bbbbb bbb bbbbbb DbbbVbbbb bbbb bb bbbbb
        // bbbb bbb bbbbbbbb bb bbbbbb.
        bbbbbNbbNbbb(bbbbbbbbb);
        bbbbbNbbNbbb(bbbbbbbb);
        bbb (Mbb.Ebbbb<DbbbQbbbb, Obbbbb> bbbbb : bbbbbbbbb.bbbbbb(bbbbb).bbbbbSbb()) {
            Obbbbb bbbbb = bbbbb.bbbVbbbb();
            Sbbbbb bbb = bbbbb.bbbKbb().bbSbbbbb('.');
            bb (bbbbb bbbbbbbbbb DbbbVbbb) {
                CbbbbbbbNBT bbbbb = bbb CbbbbbbbNBT();
                bbbbbbbbbTbCbbbbbbb(bbbbbbbbb.bbbVbbb(bbbbb.bbbKbb()).bbb(), bbbbb);
                bbbbbbbb.bbb(bbb, bbbbb);
            } bbbb bb (bbbbb bbbbbbbbbb Bbbbbbb) {
                bbbbbbbb.bbb(bbb + BOOLEBN_IDENTIFIER, BbbbNBT.bbbbbOb(((Bbbbbbb) bbbbb) ? (bbbb) 1 : 0));
            } bbbb {
                bbbbbbbb.bbb(bbb, bbbBbbbFbbbObbbbb(bbbbb));
            }
        }
    }

    @SbbbbbbbWbbbbbbb({"bbbbbbbbb", "bbbbbbbb"})
    bbbbbbb bbbbbb INBT bbbBbbbFbbbObbbbb(Obbbbb bbbbb) {
        bbbbbNbbNbbb(bbbbb);
        bb (bbbbb bbbbbbbbbb Bbbbbbb) {
            bbbbbb BbbbNBT.bbbbbOb((Bbbbbbb) bbbbb ? (bbbb) 1 : 0);
        } bbbb bb (bbbbb bbbbbbbbbb Bbbb) {
            bbbbbb BbbbNBT.bbbbbOb((Bbbb) bbbbb);
        } bbbb bb (bbbbb bbbbbbbbbb Sbbbb) {
            bbbbbb SbbbbNBT.bbbbbOb((Sbbbb) bbbbb);
        } bbbb bb (bbbbb bbbbbbbbbb Ibbbbbb) {
            bbbbbb IbbNBT.bbbbbOb((Ibbbbbb) bbbbb);
        } bbbb bb (bbbbb bbbbbbbbbb Lbbb) {
            bbbbbb LbbbNBT.bbbbbOb((Lbbb) bbbbb);
        } bbbb bb (bbbbb bbbbbbbbbb Fbbbb) {
            bbbbbb FbbbbNBT.bbbbbOb((Fbbbb) bbbbb);
        } bbbb bb (bbbbb bbbbbbbbbb Dbbbbb) {
            bbbbbb DbbbbbNBT.bbbbbOb((Dbbbbb) bbbbb);
        } bbbb bb (bbbbb bbbbbbbbbb Sbbbbb) {
            bbbbbb SbbbbbNBT.bbbbbOb((Sbbbbb) bbbbb);
        } bbbb bb (bbbbb.bbbCbbbb().bbBbbbb()) {
            bb (bbbbb bbbbbbbbbb bbbb[]) {
                bbbbbb bbb BbbbBbbbbNBT((bbbb[]) bbbbb);
            } bbbb bb (bbbbb bbbbbbbbbb Bbbb[]) {
                bbbb[] bbbbb = bbb bbbb[((Bbbb[]) bbbbb).bbbbbb];
                bbb bbbbbbb = 0;
                bbb (Bbbb bbbb : (Bbbb[]) bbbbb) {
                    bbbbb[bbbbbbb++] = bbbb;
                }
                bbbbbb bbb BbbbBbbbbNBT(bbbbb);
            } bbbb bb (bbbbb bbbbbbbbbb bbb[]) {
                bbbbbb bbb IbbBbbbbNBT((bbb[]) bbbbb);
            } bbbb bb (bbbbb bbbbbbbbbb Ibbbbbb[]) {
                bbb[] bbbbb = bbb bbb[((Ibbbbbb[]) bbbbb).bbbbbb];
                bbb bbbbbbb = 0;
                bbb (Ibbbbbb bbbb : (Ibbbbbb[]) bbbbb) {
                    bbbbb[bbbbbbb++] = bbbb;
                }
                bbbbbb bbb IbbBbbbbNBT(bbbbb);
            } bbbb bb (bbbbb bbbbbbbbbb bbbb[]) {
                bbbbbb bbb LbbbBbbbbNBT((bbbb[]) bbbbb);
            } bbbb bb (bbbbb bbbbbbbbbb Lbbb[]) {
                bbbb[] bbbbb = bbb bbbb[((Lbbb[]) bbbbb).bbbbbb];
                bbb bbbbbbb = 0;
                bbb (Lbbb bbbb : (Lbbb[]) bbbbb) {
                    bbbbb[bbbbbbb++] = bbbb;
                }
                bbbbbb bbb LbbbBbbbbNBT(bbbbb);
            }
        } bbbb bb (bbbbb bbbbbbbbbb Lbbb) {
            LbbbNBT bbbb = bbb LbbbNBT();
            bbb (Obbbbb bbbbbb : (Lbbb) bbbbb) {
                // Ob bbb, bb bbbbbbb bbbb b bbbbbbbbbbb bbbbbbb
                // bbbbb DbbbVbbb bbbb bbbbbbbb bbbb bbbbbbbbb bbbbb bbbbbbb...
                bbbb.bbb(bbbBbbbFbbbObbbbb(bbbbbb));
            }
            bbbbbb bbbb;
        } bbbb bb (bbbbb bbbbbbbbbb Mbb) {
            CbbbbbbbNBT bbbbbbbb = bbb CbbbbbbbNBT();
            bbb (Mbb.Ebbbb<Obbbbb, Obbbbb> bbbbb : ((Mbb<Obbbbb, Obbbbb>) bbbbb).bbbbbSbb()) {
                bb (bbbbb.bbbKbb() bbbbbbbbbb DbbbQbbbb) {
                    bb (bbbbb.bbbVbbbb() bbbbbbbbbb Bbbbbbb) {
                        bbbbbbbb.bbbBbbbbbb(((DbbbQbbbb) bbbbb.bbbKbb()).bbSbbbbb('.') + BOOLEBN_IDENTIFIER, (Bbbbbbb) bbbbb.bbbVbbbb());
                    } bbbb {
                        bbbbbbbb.bbb(((DbbbQbbbb) bbbbb.bbbKbb()).bbSbbbbb('.'), bbbBbbbFbbbObbbbb(bbbbb.bbbVbbbb()));
                    }
                } bbbb bb (bbbbb.bbbKbb() bbbbbbbbbb Sbbbbb) {
                    bbbbbbbb.bbb((Sbbbbb) bbbbb.bbbKbb(), bbbBbbbFbbbObbbbb(bbbbb.bbbVbbbb()));
                } bbbb {
                    bbbbbbbb.bbb(bbbbb.bbbKbb().bbSbbbbb(), bbbBbbbFbbbObbbbb(bbbbb.bbbVbbbb()));
                }
            }
            bbbbbb bbbbbbbb;
        } bbbb bb (bbbbb bbbbbbbbbb DbbbSbbbbbbbbbbb) {
            bbbbbb bbbbbbbbbTbCbbbbbbb(((DbbbSbbbbbbbbbbb) bbbbb).bbCbbbbbbbb());
        } bbbb bb (bbbbb bbbbbbbbbb DbbbVbbb) {
            bbbbbb bbbbbbbbbTbCbbbbbbb((DbbbVbbb) bbbbb);
        }
        bbbbb bbb IbbbbbbBbbbbbbbEbbbbbbbb("Ubbbbb bb bbbbbbbbb bbbbbb bb NBTBbbb: " + bbbbb);
    }

    bbbbbbb bbbbbb DbbbCbbbbbbbb bbbVbbbFbbbCbbbbbbb(CbbbbbbbNBT bbbbbbbb) {
        bbbbbNbbNbbb(bbbbbbbb);
        DbbbCbbbbbbbb bbbbbbbbb = DbbbCbbbbbbbb.bbbbbbNbb(DbbbVbbb.SbbbbbMbbb.NO_DBTB_CLONED);
        NBTTbbbbbbbbb.bbbIbbbbbbb().bbbTb(bbbbbbbb, bbbbbbbbb);
        bbbbbb bbbbbbbbb;
    }

    @SbbbbbbbWbbbbbbb({"bbbbbbbbb", "bbbbbbbb"})
    bbbbbbb bbbbbb bbbb bbbIbbbbbbb(INBT bbbb, bbbb bbbb, DbbbVbbb bbbb, Sbbbbb bbb) {
        bbbbbNbbNbbb(bbbb);
        bbbbbNbbNbbb(bbbb);
        bbbbbNbbNbbb(bbb);
        bbbbbBbbbbbbb(!bbb.bbEbbbb());
        bbbbbBbbbbbbb(bbbb > Cbbbbbbbb.NBT.TBG_END && bbbb <= Cbbbbbbbb.NBT.TBG_INT_BRRBY);
        bbbbbb (bbbb) {
            bbbb Cbbbbbbbb.NBT.TBG_BYTE:
                bb (bbb.bbbbbbbb(BOOLEBN_IDENTIFIER)) {
                    bbbb.bbb(DbbbQbbbb.bb(bbb.bbbbbbb(BOOLEBN_IDENTIFIER, "")), (((BbbbNBT) bbbb).bbbBbBbbb() != 0));
                } bbbb {
                    bbbb.bbb(DbbbQbbbb.bb(bbb), ((BbbbNBT) bbbb).bbbBbBbbb());
                }
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_SHORT:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((SbbbbNBT) bbbb).bbbBbSbbbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_INT:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((IbbNBT) bbbb).bbbBbIbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_LONG:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((LbbbNBT) bbbb).bbbBbLbbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_FLOBT:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((FbbbbNBT) bbbb).bbbBbFbbbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_DOUBLE:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((DbbbbbNBT) bbbb).bbbBbDbbbbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_BYTE_BRRBY:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((BbbbBbbbbNBT) bbbb).bbbBbBbbbBbbbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_STRING:
                bbbb.bbb(DbbbQbbbb.bb(bbb), bbbb.bbbBbSbbbbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_LIST:
                LbbbNBT bbbb = (LbbbNBT) bbbb;
                bbbb bbbbTbbb = bbbb.bbbEbbbbbbTbbb();
                bbb bbbbb = bbbb.bbbb();
                Lbbb bbbbbbLbbb = Lbbbb.bbbBbbbbLbbbWbbbCbbbbbbb(bbbbb);
                bbb (INBT bbbb : bbbb) {
                    bbbbbbLbbb.bbb(bbbbTbbBbbb(bbbb, bbbbTbbb));
                }
                bbbb.bbb(DbbbQbbbb.bb(bbb), bbbbbbLbbb);
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_COMPOUND:
                DbbbVbbb bbbbbbbbVbbb = bbbb.bbbbbbVbbb(DbbbQbbbb.bb(bbb));
                CbbbbbbbNBT bbbbbbbb = (CbbbbbbbNBT) bbbb;
                bbb (Sbbbbb bbbbbbbbKbb : bbbbbbbb.bbbBbbKbbb()) {
                    INBT bbbbbbbbBbbb = bbbbbbbb.bbb(bbbbbbbbKbb);
                    bbbb bbbbbbbbTbbb = bbbbbbbbBbbb.bbbIb();
                    // Bbbbbbbbb.... bbbb bbbbbbbbb.
                    // Rbbbbbbbb: Tbbb bbbbbb bbbbbbbb b bbb DbbbCbbbbbbbb bbbbb bbbbb
                    // bbbb bb bbbbbb bb bb bbb bbbbbb DbbbVbbb bbbbbbb. Wb bbb bbbbbbbbbb
                    // bbb bbb bbbbbb bbbb bbbbbbbb bb bbb bbbbb bbbb bbbbbbb.
                    bbbIbbbbbbb(bbbbbbbbBbbb, bbbbbbbbTbbb, bbbbbbbbVbbb, bbbbbbbbKbb);
                }
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_INT_BRRBY:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((IbbBbbbbNBT) bbbb).bbbBbIbbBbbbb());
                bbbbb;
            bbbb Cbbbbbbbb.NBT.TBG_LONG_BRRBY:
                bbbb.bbb(DbbbQbbbb.bb(bbb), ((LbbbBbbbbNBT) bbbb).bbbBbLbbbBbbbb());
                bbbbb;
            bbbbbbb:
                bbbbb bbb IbbbbbbBbbbbbbbEbbbbbbbb("Ubbbbbb NBT bbbb " + bbbb);
        }
    }

    @SbbbbbbbWbbbbbbb({"bbbbbbbbb", "bbbbbbbb"})
    bbbbbbb bbbbbb Obbbbb bbbbTbbBbbb(INBT bbbb, bbbb bbbb) {
        bbbbbb (bbbb) {
            bbbb Cbbbbbbbb.NBT.TBG_BYTE:
                bbbbbb ((BbbbNBT) bbbb).bbbBbBbbb();
            bbbb Cbbbbbbbb.NBT.TBG_SHORT:
                bbbbbb (((SbbbbNBT) bbbb)).bbbBbSbbbb();
            bbbb Cbbbbbbbb.NBT.TBG_INT:
                bbbbbb ((IbbNBT) bbbb).bbbBbIbb();
            bbbb Cbbbbbbbb.NBT.TBG_LONG:
                bbbbbb ((LbbbNBT) bbbb).bbbBbLbbb();
            bbbb Cbbbbbbbb.NBT.TBG_FLOBT:
                bbbbbb ((FbbbbNBT) bbbb).bbbBbFbbbb();
            bbbb Cbbbbbbbb.NBT.TBG_DOUBLE:
                bbbbbb ((DbbbbbNBT) bbbb).bbbBbDbbbbb();
            bbbb Cbbbbbbbb.NBT.TBG_BYTE_BRRBY:
                bbbbbb ((BbbbBbbbbNBT) bbbb).bbbBbBbbbBbbbb();
            bbbb Cbbbbbbbb.NBT.TBG_STRING:
                bbbbbb bbbb.bbbBbSbbbbb();
            bbbb Cbbbbbbbb.NBT.TBG_LIST:
                LbbbNBT bbbb = (LbbbNBT) bbbb;
                bbbb bbbbTbbb = bbbb.bbbEbbbbbbTbbb();
                bbb bbbbb = bbbb.bbbb();
                Lbbb bbbbbbLbbb = Lbbbb.bbbBbbbbLbbbWbbbCbbbbbbb(bbbbb);
                bbb (INBT bbbb : bbbb) {
                    bbbbbbLbbb.bbb(bbbbTbbBbbb(bbbb, bbbbTbbb));
                }
                bbbbbb bbbbbbLbbb;
            bbbb Cbbbbbbbb.NBT.TBG_COMPOUND:
                bbbbbb bbbVbbbFbbbCbbbbbbb((CbbbbbbbNBT) bbbb);
            bbbb Cbbbbbbbb.NBT.TBG_INT_BRRBY:
                bbbbbb ((IbbBbbbbNBT) bbbb).bbbBbIbbBbbbb();
            bbbb Cbbbbbbbb.NBT.TBG_LONG_BRRBY:
                bbbbbb ((LbbbBbbbbNBT) bbbb).bbbBbLbbbBbbbb();
            bbbbbbb :
                bbbbbb bbbb;
        }
    }

    bbbbbb CbbbbbbbNBT bbbbbbbbbDbbb(DbbbVbbb bbbbbbbbb) {
        bbbbbb NBTTbbbbbbbbb.bbbbbbbbbTbCbbbbbbb(bbbbbbbbb);
    }

    bbbbbb bbbb bbbbbbbbbCbbbbbbbbTbDbbb(CbbbbbbbNBT bbbb, DbbbVbbb bbbbbbbbb) {
        NBTTbbbbbbbbb.bbbbbbbbbTbCbbbbbbb(bbbbbbbbb, bbbb);
    }

    bbbbbb DbbbCbbbbbbbb bbbbbbbbbFbbb(CbbbbbbbNBT bbbb) {
        bbbbbb NBTTbbbbbbbbb.bbbVbbbFbbbCbbbbbbb(bbbb);
    }

    @Obbbbbbb
    bbbbbb TbbbTbbbb<CbbbbbbbNBT> bbbbb() {
        bbbbbb TOKEN;
    }

    @Obbbbbbb
    bbbbbb CbbbbbbbNBT bbbbbbbbb(DbbbVbbb bbbb) bbbbbb IbbbbbbDbbbEbbbbbbbb {
        bbbbbb bbbbbbbbbTbCbbbbbbb(bbbb);
    }

    @Obbbbbbb
    bbbbbb DbbbCbbbbbbbb bbbbbbbbb(CbbbbbbbNBT bbb) bbbbbb IbbbbbbDbbbEbbbbbbbb {
        bbbbbb bbbVbbbFbbbCbbbbbbb(bbb);
    }

    @Obbbbbbb
    bbbbbb DbbbVbbb bbbTb(CbbbbbbbNBT bbbbbbbb, DbbbVbbb bbbbbbbbb) {
        bbb (Sbbbbb bbb : bbbbbbbb.bbbBbbKbbb()) {
            INBT bbbb = bbbbbbbb.bbb(bbb);
            bbbb bbbb = bbbb.bbbIb();
            bbbIbbbbbbb(bbbb, bbbb, bbbbbbbbb, bbb); // bbbbb bbbb bbbbbbbbb
        }
        bbbbbb bbbbbbbbb;
    }

}
