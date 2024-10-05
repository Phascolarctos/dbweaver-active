package top.monkeyfans.active;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;


public class LMLicense implements LMSerializable {
    private static final Logger log = Logger.getLogger("LMLicense");
    private static final long DAY_MS = 86400000L;
    public static final long FLAG_NONE = 0L;
    public static final long FLAG_ROLE_BASED = 1L;
    public static final long FLAG_CANCELED = 2L;
    public static final long FLAG_RESELLER = 4L;
    public static final long FLAG_SUBSCRIPTION = 8L;
    public static final long FLAG_LIMITED = 16L;
    public static final long FLAG_LIMITED_VERSION = 32L;
    public static final long FLAG_SERVER_LICENSE = 64L;
    public static final long FLAG_UNLIMITED_USERS = 256L;
    public static final long FLAG_UNLIMITED_TIME = 512L;
    public static final long FLAG_UNLIMITED_SERVERS = 1024L;
    public static final long FLAG_MULTI_INSTANCE = 2048L;
    public static final byte UPDATE_HASH_FORMAT_SIMPLE = 1;
    private transient byte[] encoded;
    private final String licenseId;
    private final LMLicenseType licenseType;
    private final Date licenseIssueTime;
    private final Date licenseStartTime;
    private final Date licenseEndTime;
    private long flags;
    private final String productId;
    private final String productVersion;
    private final String ownerId;
    private final String ownerCompany;
    private final String ownerName;
    private String ownerEmail;
    private byte yearsNumber;
    private byte reserved1;
    private short usersNumber;
    private short serversNumber;
    private final List<LMLicenseRole> roles = new ArrayList<>();
    private final List<LMGroupUser> groupUsers = new ArrayList<>();
    private LMLicenseFormat licenseFormat;
    private LMStatusDetails remoteStatus;
    private LMLicenseStatus status = LMLicenseStatus.VALID;

    public LMLicense(byte[] encryptedData, Key key) throws LMException {
        this.encoded = encryptedData;

        ByteBuffer buffer;
        try {
            buffer = ByteBuffer.wrap(LMEncryption.decrypt(encryptedData, key));
        } catch (LMException var12) {
            throw new LMException("Corrupted license text:\n" + var12.getMessage());
        }

        try {
            this.licenseFormat = LMLicenseFormat.valueOf(buffer.get());
        } catch (Exception var11) {
            log.warning("Unsupported license format");
            this.licenseFormat = LMLicenseFormat.STANDARD;
        }

        if (buffer.capacity() < this.licenseFormat.getEncryptedLength()) {
            throw new LMException("Bad " + this.licenseFormat + " license length (" + buffer.capacity() + ")");
        } else {
            this.licenseId = LMUtils.getStringFromBuffer(buffer, 16);
            this.licenseType = LMLicenseType.valueOf(buffer.get());
            this.licenseIssueTime = LMUtils.getDateFromBuffer(buffer);
            this.licenseStartTime = LMUtils.getDateFromBuffer(buffer);
            this.licenseEndTime = LMUtils.getDateFromBuffer(buffer);
            this.flags = buffer.getLong();
            if (!this.isMultiInstance() && buffer.capacity() != this.licenseFormat.getEncryptedLength()) {
                throw new LMException("Bad " + this.licenseFormat + " license length (" + buffer.capacity() + ")");
            } else {
                this.productId = LMUtils.getStringFromBuffer(buffer, 16);
                this.productVersion = LMUtils.getStringFromBuffer(buffer, 8);
                this.ownerId = LMUtils.getStringFromBuffer(buffer, 16);
                this.ownerCompany = LMUtils.getStringFromBuffer(buffer, 64);
                if (this.licenseFormat == LMLicenseFormat.STANDARD) {
                    this.ownerName = LMUtils.getStringFromBuffer(buffer, 64);
                } else {
                    this.ownerName = LMUtils.getStringFromBuffer(buffer, 32);
                    this.ownerEmail = LMUtils.getStringFromBuffer(buffer, 48);
                    this.yearsNumber = (byte) Math.max(1, buffer.get());
                    this.reserved1 = buffer.get();
                    this.usersNumber = (short) Math.max(1, buffer.getShort());
                }

                if (this.licenseFormat == LMLicenseFormat.ADVANCED) {
                    buffer.get();
                    buffer.get();
                    byte roleCount = buffer.get();

                    for (int i = 0; i < roleCount; i++) {
                        String roleId = LMUtils.getStringFromBuffer(buffer, 16);

                        try {
                            LMRole lmRole = LMRole.valueOf(roleId);
                            int userCount = buffer.getInt();
                            LMLicenseRole role = new LMLicenseRole(lmRole, userCount);
                            this.roles.add(role);
                        } catch (IllegalArgumentException var10) {
                            log.severe("Role '" + roleId + "' not recognized");
                        }
                    }
                }

                if (this.isMultiInstance()) {
                    buffer.get();
                    buffer.get();
                    this.setServersNumber((short) Math.max(1, buffer.getShort()));
                    int usersSize = buffer.getInt();

                    for (int i = 0; i < usersSize; i++) {
                        buffer.get();
                        String userEmail = LMUtils.getStringFromBuffer(buffer, 128);
                        String userName = LMUtils.getStringFromBuffer(buffer, 128);
                        LMRole licenseRole = null;
                        if (this.licenseFormat == LMLicenseFormat.ADVANCED) {
                            String roleId = LMUtils.getStringFromBuffer(buffer, 16);

                            try {
                                licenseRole = LMRole.valueOf(roleId);
                            } catch (Exception var13) {
                                log.severe("Role '" + roleId + "' not recognized for user " + userEmail);
                                continue;
                            }
                        }

                        LMGroupUser groupUser = new LMGroupUser(userEmail, userName, licenseRole);
                        this.groupUsers.add(groupUser);
                    }
                }
            }
        }
    }

    public LMLicense(
            String licenseId,
            LMLicenseType licenseType,
            Date licenseIssueTime,
            Date licenseStartTime,
            Date licenseEndTime,
            long flags,
            String productId,
            String productVersion,
            String ownerId,
            String ownerCompany,
            String ownerName,
            String ownerEmail
    ) {
        this(
                licenseId,
                licenseType,
                licenseIssueTime,
                licenseStartTime,
                licenseEndTime,
                flags,
                productId,
                productVersion,
                ownerId,
                ownerCompany,
                ownerName,
                ownerEmail,
                LMLicenseStatus.VALID
        );
    }

    public LMLicense(
            String licenseId,
            LMLicenseType licenseType,
            Date licenseIssueTime,
            Date licenseStartTime,
            Date licenseEndTime,
            long flags,
            String productId,
            String productVersion,
            String ownerId,
            String ownerCompany,
            String ownerName,
            String ownerEmail,
            LMLicenseStatus status
    ) {
        this.licenseFormat = (flags & 1L) != 0L ? LMLicenseFormat.ADVANCED : LMLicenseFormat.EXTENDED;
        this.licenseId = licenseId;
        this.licenseType = licenseType;
        this.licenseIssueTime = licenseIssueTime;
        this.licenseStartTime = licenseStartTime;
        this.licenseEndTime = licenseEndTime;
        this.flags = flags;
        this.productId = productId;
        this.productVersion = productVersion;
        this.ownerId = ownerId;
        this.ownerCompany = ownerCompany;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.yearsNumber = 1;
        this.reserved1 = 0;
        this.usersNumber = 1;
        this.status = status;
    }

    public byte[] getEncoded() {
        return this.encoded;
    }


    public LMLicenseFormat getFormat() {
        return this.licenseFormat;
    }

    public void setLicenseFormat(LMLicenseFormat licenseFormat) {
        this.licenseFormat = licenseFormat;
    }


    public String getLicenseId() {
        return this.licenseId;
    }


    public LMLicenseType getLicenseType() {
        return this.licenseType;
    }


    public Date getLicenseIssueTime() {
        return this.licenseIssueTime;
    }


    public Date getLicenseStartTime() {
        return this.licenseStartTime;
    }

    public Date getLicenseEndTime() {
        return this.licenseEndTime == null && this.licenseType == LMLicenseType.ACADEMIC
                ? new Date(this.licenseStartTime.getTime() + 31536000000L)
                : this.licenseEndTime;
    }

    public Date getCalculatedLicenseEndTime() {
        Date licenseEndTime = this.getLicenseEndTime();
        if (licenseEndTime == null) {
            licenseEndTime = new Date(this.licenseStartTime.getTime() + 86400000L * (long) this.getYearsNumber() * 365L);
        }

        return licenseEndTime;
    }

    public long getFlags() {
        return this.flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }


    public String getProductId() {
        return this.productId;
    }


    public String getProductVersion() {
        return this.productVersion;
    }


    public String getOwnerId() {
        return this.ownerId;
    }

    public String getOwnerCompany() {
        return this.ownerCompany;
    }

    public String getOwnerName() {
        return this.ownerName;
    }


    public String getOwnerEmail() {
        return this.ownerEmail;
    }

    public byte getYearsNumber() {
        return this.yearsNumber;
    }

    public void setYearsNumber(byte yearsNumber) {
        this.yearsNumber = yearsNumber;
    }

    public byte getReserved1() {
        return this.reserved1;
    }

    public short getUsersNumber() {
        return this.usersNumber;
    }

    public void setUsersNumber(short usersNumber) {
        this.usersNumber = usersNumber;
    }

    public void setServersNumber(short serversNumber) {
        this.serversNumber = serversNumber;
    }

    public short getServersNumber() {
        return this.serversNumber;
    }

    public List<LMLicenseRole> getRoles() {
        return this.roles;
    }

    public void setRoles(List<LMLicenseRole> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void setGroupUsers(List<LMGroupUser> groupUsers) {
        this.groupUsers.clear();
        this.groupUsers.addAll(groupUsers);
    }

    public List<LMGroupUser> getGroupUsers() {
        return this.groupUsers;
    }

    public String getOwnerFull() {
        String licensedTo = this.ownerName;
        if (this.ownerCompany != null && !this.ownerCompany.isEmpty()) {
            licensedTo = this.ownerCompany + " / " + licensedTo;
        }

        return licensedTo;
    }


    @Override
    public byte[] getData() {
        LMByteArrayOutputStream outBuffer = new LMByteArrayOutputStream(this.licenseFormat.getEncryptedLength());
        outBuffer.write(this.licenseFormat.getId());
        LMUtils.writeStringToBuffer(outBuffer, this.licenseId, 16);
        outBuffer.write(this.licenseType.getId());
        LMUtils.writeDateToBuffer(outBuffer, this.licenseIssueTime);
        LMUtils.writeDateToBuffer(outBuffer, this.licenseStartTime);
        LMUtils.writeDateToBuffer(outBuffer, this.licenseEndTime);
        LMUtils.writeLongToBuffer(outBuffer, this.flags);
        LMUtils.writeStringToBuffer(outBuffer, this.productId, 16);
        LMUtils.writeStringToBuffer(outBuffer, this.productVersion, 8);
        LMUtils.writeStringToBuffer(outBuffer, this.ownerId, 16);
        LMUtils.writeStringToBuffer(outBuffer, this.ownerCompany, 64);
        if (this.licenseFormat == LMLicenseFormat.STANDARD) {
            LMUtils.writeStringToBuffer(outBuffer, this.ownerName, 64);
        } else {
            LMUtils.writeStringToBuffer(outBuffer, this.ownerName, 32);
            LMUtils.writeStringToBuffer(outBuffer, this.ownerEmail, 48);
            outBuffer.write(this.yearsNumber);
            outBuffer.write(this.reserved1);
            LMUtils.writeShortToBuffer(outBuffer, this.usersNumber);
            if (this.licenseFormat == LMLicenseFormat.ADVANCED) {
                outBuffer.write(0);
                outBuffer.write(0);
                outBuffer.write(this.roles.size());

                for (LMLicenseRole role : this.roles) {
                    LMUtils.writeStringToBuffer(outBuffer, role.getRole().name(), 16);
                    LMUtils.writeIntToBuffer(outBuffer, role.getUsersNumber());
                }
            }
        }

        if (this.isMultiInstance()) {
            outBuffer.write(0);
            outBuffer.write(0);
            LMUtils.writeShortToBuffer(outBuffer, this.serversNumber);
            LMUtils.writeIntToBuffer(outBuffer, this.getGroupUsers().size());

            for (LMGroupUser groupUser : this.getGroupUsers()) {
                outBuffer.write(0);
                LMUtils.writeStringToBuffer(outBuffer, groupUser.getEmail(), 128);
                LMUtils.writeStringToBuffer(outBuffer, groupUser.getUserName(), 128);
                if (this.licenseFormat == LMLicenseFormat.ADVANCED) {
                    LMUtils.writeStringToBuffer(outBuffer, groupUser.getLicenseRole().name(), 16);
                }
            }
        }

        return outBuffer.getBuffer();
    }

    public String getDataHash() {
        byte[] data = this.getData();
        return this.makeMD5Digest(data);
    }

    public String getUpdateRequestHash() {
        byte[] data = new byte[25];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        LMUtils.putStringToBuffer(buffer, this.licenseId, 16);
        buffer.put(this.licenseType.getId());
        long timestampSeconds = (this.licenseIssueTime.getTime() + 999L) / 1000L * 1000L;
        LMUtils.putDateToBuffer(buffer, new Date(timestampSeconds));
        return "1:" + this.makeMD5Digest(data);
    }


    private String makeMD5Digest(byte[] data) {
        try {
            byte[] md5hash = MessageDigest.getInstance("MD5").digest(data);
            return LMUtils.toHexString(md5hash).toLowerCase(Locale.ENGLISH).trim();
        } catch (NoSuchAlgorithmException var8) {
            log.severe("No MD5 digest. Use simple sum.");
            long sum = 0L;

            for (byte b : data) {
                sum += (long) b;
            }

            return Long.toHexString(sum);
        }
    }

    @Override
    public String toString() {
        return "licenseId="
                + this.licenseId
                + "\nlicenseType="
                + this.licenseType
                + "\nlicenseIssueTime="
                + this.licenseIssueTime
                + "\nlicenseStartTime="
                + this.licenseStartTime
                + "\nlicenseEndTime="
                + this.licenseEndTime
                + "\nflags="
                + this.flags
                + "\nproductId="
                + this.productId
                + "\nproductVersion="
                + this.productVersion
                + "\nownerId="
                + this.ownerId
                + "\nownerCompany="
                + this.ownerCompany
                + "\nownerName="
                + this.ownerName
                + "\nownerEmail="
                + this.ownerEmail
                + "\nyearsNumber="
                + this.yearsNumber
                + "\nusersNumber="
                + this.usersNumber;
    }

    public boolean isSubscription() {
        return (this.flags & 8L) == 8L;
    }

    public boolean isValidForProduct(LMProduct product) {
        return this.productId.equals(product.getId()) || LMUtils.contains(product.getUmbrellaProducts(), this.productId);
    }

    public boolean isValidFor(LMProduct product, LMProductRelease productRelease, boolean checkExpired) {
        if (!this.isValidForProduct(product)) {
            return false;
        } else if (this.isSubscription()) {
            return true;
        } else {
            if (checkExpired && this.licenseType == LMLicenseType.TEAM) {
                Date licenseEndTime = new Date(this.licenseStartTime.getTime() + 86400000L * (long) (365 * this.yearsNumber + 14));
                if (licenseEndTime.compareTo(new Date()) < 0) {
                    return true;
                }
            }

            if ((this.flags & 32L) == 32L && !Objects.equals(this.getProductVersion(), product.getVersion())) {
                return false;
            } else if (this.getLicenseType() == LMLicenseType.ULTIMATE) {
                return true;
            } else {
                return checkExpired && this.isExpired() ? false : !this.isOutOfSupport(product, productRelease);
            }
        }
    }

    public boolean isMultiInstance() {
        return (this.flags & 2048L) != 0L;
    }

    public boolean isExpired() {
        Date curDate = new Date();
        Date licenseEndTime = this.getLicenseEndTime();
        switch (this.getLicenseType()) {
            case STANDARD:
            case TRIAL:
            case ACADEMIC:
                if (licenseEndTime == null) {
                    licenseEndTime = new Date(this.licenseStartTime.getTime() + 31536000000L);
                }
            default:
                return licenseEndTime != null && licenseEndTime.compareTo(curDate) < 0;
        }
    }

    private boolean isOutOfSupport(LMProduct product, LMProductRelease productRelease) {
        if (this.getLicenseType() == LMLicenseType.TRIAL) {
            return this.isExpired();
        } else {
            Date releaseDate = productRelease == null ? product.getReleaseDate() : productRelease.getReleaseDate();
            Date licenseEndTime = this.getSupportEndDate();
            if (licenseEndTime.compareTo(releaseDate) < 0) {
                Date curDate = new Date();
                return curDate.compareTo(releaseDate) < 0 ? this.isExpired() : true;
            } else {
                return false;
            }
        }
    }

    public boolean isCancelled() {
        return this.status.equals(LMLicenseStatus.CANCELED);
    }

    public LMStatusDetails getLicenseStatus(LMProduct product) {
        if (!this.isValidForProduct(product)) {
            return new LMStatusDetails("Wrong product", "You can't use this license with product '" + product.getName() + "'", false);
        } else if (this.isSubscription()) {
            return this.remoteStatus != null ? this.remoteStatus : new LMStatusDetails("Subscription", "Valid subscription", true);
        } else if ((this.flags & 32L) == 32L && !Objects.equals(this.getProductVersion(), product.getVersion())) {
            return new LMStatusDetails(
                    "Only version " + this.getProductVersion() + " supported",
                    "Your license is limited to the version " + product.getName() + " " + this.getProductVersion() + " and can't be used with other produce versions",
                    false
            );
        } else if (this.getLicenseType() == LMLicenseType.ULTIMATE) {
            return new LMStatusDetails("Ultimate license", "Ultimate license", true);
        } else if (this.isExpired()) {
            return new LMStatusDetails(
                    "Expired (" + LMUtils.HR_DATE_FORMAT.format(this.getExpireDate()) + ")",
                    "Your license has been expired at " + LMUtils.HR_DATE_FORMAT.format(this.getExpireDate()),
                    false
            );
        } else if (this.isCancelled()) {
            return new LMStatusDetails(LMLicenseStatus.CANCELED.toString(), "Your license has been canceled.", false);
        } else if (this.isOutOfSupport(product, null)) {
            return new LMStatusDetails(
                    "Not valid for " + product.getVersion(),
                    "Your license is not valid for "
                            + product.getName()
                            + " "
                            + product.getVersion()
                            + ".\nProduct release date ("
                            + LMUtils.HR_DATE_FORMAT.format(product.getReleaseDate())
                            + ") is later than the license end-of-support date ("
                            + LMUtils.HR_DATE_FORMAT.format(this.getSupportEndDate())
                            + ").\nYou can use earlier versions or upgrade your license.",
                    false
            );
        } else {
            return !this.isValidFor(product, null, false)
                    ? new LMStatusDetails("Invalid license", "This license is not applicable", false)
                    : new LMStatusDetails("Valid", "Valid license", true);
        }
    }

    public Date getExpireDate() {
        Date licenseEndTime = this.getLicenseEndTime();
        switch (this.getLicenseType()) {
            case STANDARD:
            case TRIAL:
            case ACADEMIC:
                if (licenseEndTime == null) {
                    licenseEndTime = new Date(this.licenseStartTime.getTime() + 31536000000L);
                }
            default:
                return licenseEndTime;
        }
    }


    public Date getSupportEndDate() {
        if (this.licenseEndTime != null) {
            return this.licenseEndTime;
        } else {
            Calendar cl = Calendar.getInstance();
            if (this.licenseStartTime != null) {
                cl.setTime(this.licenseStartTime);
            } else {
                cl.setTime(this.licenseIssueTime);
            }

            int yearsNumber = this.getYearsNumber();
            if (yearsNumber <= 0) {
                yearsNumber = 1;
            }

            cl.add(1, yearsNumber);
            return cl.getTime();
        }
    }

    public String getLicenseTypeFull() {
        return this.isSubscription() ? "Subscription" : this.licenseType.getDisplayName();
    }

    public LMStatusDetails getRemoteStatus() {
        return this.remoteStatus;
    }

    public void setRemoteStatus(LMStatusDetails remoteStatus) {
        this.remoteStatus = remoteStatus;
    }
}
