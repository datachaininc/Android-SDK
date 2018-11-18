package in.datacha.classes;

class ReuestPayload {
    private String raw;
    private String signed;
    private String key;
    private String signed_key;

     ReuestPayload(String raw, String signed, String key, String signed_key) {
        this.raw = raw;
        this.signed = signed;
        this.key = key;
        this.signed_key = signed_key;
    }

     ReuestPayload() {
    }

     String getRaw() {
        return raw;
    }

     void setRaw(String raw) {
        this.raw = raw;
    }

     String getSigned() {
        return signed;
    }

     void setSigned(String signed) {
        this.signed = signed;
    }

     String getKey() {
        return key;
    }

     void setKey(String key) {
        this.key = key;
    }

     String getSigned_key() {
        return signed_key;
    }

     void setSigned_key(String signed_key) {
        this.signed_key = signed_key;
    }
}
