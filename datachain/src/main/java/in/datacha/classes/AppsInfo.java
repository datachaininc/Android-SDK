package in.datacha.classes;

class AppsInfo{
        private String name;
        private String packageName;
        private String category;

         AppsInfo() {
        }

         AppsInfo(String name, String packageName) {
            this.name = name;
            this.packageName = packageName;
        }

        AppsInfo(String name, String packageName, String category) {
            this.name = name;
            this.packageName = packageName;
            this.category = category;
        }

         String getName() {
            return name;
        }

         void setName(String name) {
            this.name = name;
        }

         String getPackageName() {
            return packageName;
        }

         void setPackageName(String packageName) {
            this.packageName = packageName;
        }

         String getCategory() {
            return category;
        }

         void setCategory(String category) {
            this.category = category;
        }}

