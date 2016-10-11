package ng.abdlquadri.pastes;

/**
 *
 * @author abdlquadri
 */
public final class Constants {

    public static final String ADDRESS_PUBLIC_ENTRY = "entry.public";

    private Constants() {}

  /** API Route */
  public static final String API_GET = "/entries/:entryId";
  public static final String API_LIST_ALL = "/entries";
  public static final String API_CREATE = "/entries";
  public static final String API_UPDATE = "/entries/:entryId";
  public static final String API_DELETE = "/entries/:entryId";
  public static final String API_DELETE_ALL = "/entries";
  public static final String API_WEB_SOCKET = "/latest";


}
