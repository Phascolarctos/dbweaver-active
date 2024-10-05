package top.monkeyfans.active;

import java.util.List;
import java.util.Set;

public class LMServiceConstants {
   public static final short TRIAL_LICENSE_DURATION = 1;
   public static final short TRIAL_LICENSE_USER_COUNT = 1;
   public static final short TRIAL_CB_LICENSE_USER_COUNT = 5;
   public static final Set<String> TE_LICENSES = Set.of("dbeaver-team", "cloudbeaver-dc");
   public static final String CB_LICENSE = "cloudbeaver-ee";
   public static final List<LMLicenseRole> TRIAL_TE_ROLES = List.of(
      new LMLicenseRole(LMRole.ADMINISTRATOR, 1),
      new LMLicenseRole(LMRole.DEVELOPER, 3),
      new LMLicenseRole(LMRole.MANAGER, 3),
      new LMLicenseRole(LMRole.EDITOR, 3),
      new LMLicenseRole(LMRole.VIEWER, 3)
   );
   public static final long SUBSCRIPTION_RENEW_GAP_DAYS = 3L;
}
