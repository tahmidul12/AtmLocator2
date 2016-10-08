package modelClasses;

/**
 * Created by Tahmidul on 10/8/2016.
 */

public class BankModel {
        private String bid;
        private String bname;
        private String batmname;
        private String blat;
        private String blongi;
        private String baddress;
        private String bcity;
        private String bstate;
        private String bcountry;


        public BankModel(String bid, String bname, String batmname, String blat, String blongi, String baddress, String bcity, String bstate
            , String bcountry) {
        this.bid = bid;
        this.bname = bname;
        this.batmname = batmname;
        this.blat = blat;
        this.blongi = blongi;
        this.baddress = baddress;
        this.bcity = bcity;
        this.bstate = bstate;
        this.bcountry = bcountry;

    }
    public BankModel(String bname, String batmname, String blat, String blongi, String baddress, String bcity, String bstate
            , String bcountry) {
        this.bname = bname;
        this.batmname = batmname;
        this.blat = blat;
        this.blongi = blongi;
        this.baddress = baddress;
        this.bcity = bcity;
        this.bstate = bstate;
        this.bcountry = bcountry;

    }


        public String getBid() {
            return bid;
        }

        public String getBname() {
            return bname;
        }

        public String getBatmname() {
            return batmname;
        }

        public String getBlat() {
            return blat;
        }
        public String getBlongi() {
            return blongi;
        }

        public String getBaddress() {
            return baddress;
        }

        public String getBcity() {
            return bcity;
        }

        public String getBstate() {
            return bstate;
        }
        public String getBcountry() {
            return bcountry;
        }



        public void setBid(String nshort) {
            this.bid = nshort;
        }

        public void setBname(String ndetail) {
            this.bname = ndetail;
        }

        public void setBatmname(String imid) {
            this.batmname = imid;
        }

        public void setBlat(String imurl) {
            this.blat = imurl;
        }

        public void setBlongi(String nshort) {
            this.blongi = nshort;
        }

        public void setBaddress(String ndetail) {
            this.baddress = ndetail;
        }

        public void setBcity(String imid) {
            this.bcity = imid;
        }

        public void setBstate(String imurl) {
            this.bstate = imurl;
        }
        public void setBcountry(String imurl) {
            this.bcountry = imurl;
        }


}
