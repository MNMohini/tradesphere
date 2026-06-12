public class User {

    @Document
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class User {
        private String id;
        private String name;
        @Id
        private String email;
        private String password;
        private String role;
        private long phoneNumber;
}