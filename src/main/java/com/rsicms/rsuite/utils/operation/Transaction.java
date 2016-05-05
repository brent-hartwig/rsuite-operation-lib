package com.rsicms.rsuite.utils.operation;

import java.util.HashMap;
import java.util.Map;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.VersionSpecifier;
import com.reallysi.rsuite.api.control.ObjectDestroyOptions;
import com.reallysi.rsuite.api.control.ObjectRollbackOptions;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.workflow.ingest.Sandbox;
import com.rsicms.rsuite.utils.mo.MOUtils;
import com.rsicms.rsuite.utils.operation.result.OperationResult;

/**
 * Use this class to keep track of objects created, modified, or deleted by what is considered a
 * single operation. While the class does not support all of this now, it may be extended to do so.
 * It does support new and updated MOs, and offers a method to rollback those changes.
 */
public class Transaction
    extends Sandbox {

  /**
   * Updated MOs
   */
  private Map<String, String> updatedAssets = new HashMap<String, String>();

  /**
   * New MOs destroyed by rollback request.
   */
  private Map<String, String> newAssetsRolledBack = new HashMap<String, String>();

  /**
   * Updated MOs reverted by rollback request.
   */
  private Map<String, String> updatedAssetsRolledBack = new HashMap<String, String>();

  /**
   * Flag indicated if rollback was requested.
   */
  private boolean rollbackRequested;

  /**
   * Optional properties for the transaction, where the map key is the prop name, and the map value
   * is the prop value.
   */
  private Map<String, String> props = new HashMap<String, String>();

  /**
   * The one and only constructor.
   */
  public Transaction() {
    super();
    this.rollbackRequested = false;
  }

  /**
   * allow to save the name of the asset in the asset map (new assets created)
   * 
   * @param moId
   * @param assetName
   */
  public void addAsset(
      String moId,
      String assetName) {
    getAssetsLoaded().put(
        moId,
        assetName);
  }

  /**
   * allow to save the name of the asset in the asset map (assets updated)
   * 
   * @param moId
   * @param assetName
   */
  public void addUpdatedAsset(
      String moId,
      String assetName) {
    getUpdatedAssets().put(
        moId,
        assetName);
  }

  /**
   * return the map of assets updated
   * 
   * @return the map of assets updated
   */
  public Map<String, String> getUpdatedAssets() {
    return updatedAssets;
  }

  /**
   * Assets created by this transaction, but subsequently rolled back.
   * 
   * @return Assets created by this transaction, but subsequently rolled back. Key is the MO ID.
   *         Value is a label for the MO.
   */
  public Map<String, String> getNewAssetsRolledBack() {
    return newAssetsRolledBack;
  }

  /**
   * Assets updated by this transaction, but subsequently rolled back.
   * 
   * @return Assets updated by this transaction, but subsequently rolled back. Key is the MO ID.
   *         Value is a label for the MO.
   */
  public Map<String, String> getUpdatedAssetsRolledBack() {
    return updatedAssetsRolledBack;
  }

  /**
   * Find out if rollback was requested for this transaction.
   * 
   * @return True if rollback was requested.
   */
  public boolean wasRollbackRequested() {
    return rollbackRequested;
  }

  /**
   * Set a property on the transaction.
   * 
   * @param name
   * @param value
   */
  public void setProperty(
      String name,
      String value) {
    props.put(
        name,
        value);
  }

  /**
   * Get a property from the transaction.
   * 
   * @param name
   * @return property value or null if property was never set (or prop value is null)
   */
  public String getProperty(
      String name) {
    return props.get(name);
  }

  /**
   * Rollback everything known to this transaction.
   * <p>
   * New MOs will be deleted.
   * <p>
   * Updated MOs will be rolled back to the previous version.
   * <p>
   * Versioned LMD is not yet supported.
   * 
   * @param context
   * @param user
   * @param result
   */
  public void rollback(
      ExecutionContext context,
      User user,
      OperationResult result) {

    this.rollbackRequested = true;

    // Rollback this transaction's new assets (destroy)
    rollbackNewAssets(
        context,
        user,
        result,
        getAssetsLoaded(),
        newAssetsRolledBack);

    // Rollback this transaction's updated assets (rollback to previous version)
    rollbackUpdatedAssets(
        context,
        user,
        result,
        getUpdatedAssets(),
        updatedAssetsRolledBack);

  }

  /**
   * Rollback the provided map of assets by destroying them.
   * 
   * @param context
   * @param user
   * @param result Info- and warning-level messages may be added by this method.
   * @param assetsToProcess Map where the key is the MO ID to destroy, and the value is the label to
   *        use for the MO.
   * @param processedAssets Map populated by this method, identifying the MOs it was able to
   *        destroy. Same key-value use as the above map.
   */
  private static void rollbackNewAssets(
      ExecutionContext context,
      User user,
      OperationResult result,
      Map<String, String> assetsToProcess,
      Map<String, String> processedAssets) {

    String rollbackLabel = OperationMessageProperties.get("rollback.label");

    // Bail if there are no assets to process.
    if (assetsToProcess == null || assetsToProcess.size() == 0) {
      result.addInfoMessage(
          rollbackLabel,
          OperationMessageProperties.get("rollback.info.no.new.assets"));
      return;
    }

    result.addInfoMessage(
        rollbackLabel,
        OperationMessageProperties.get(
            "rollback.info.processing.new.assets",
            assetsToProcess.size()));

    // Attempt to destroy each MO given to us.
    ObjectDestroyOptions destroyOptions = new ObjectDestroyOptions();
    for (Map.Entry<String, String> entry : assetsToProcess.entrySet()) {
      String id = entry.getKey();
      String label = entry.getValue();
      try {
        result.addInfoMessage(
            rollbackLabel,
            OperationMessageProperties.get(
                "rollback.info.processing.new.asset",
                label,
                id));

        // Make sure the current user has or can check out the MO.
        MOUtils.checkout(
            context,
            user,
            id);

        // Attempt destroy.
        context.getManagedObjectService().destroy(
            user,
            id,
            destroyOptions);

        // Add to processed list
        processedAssets.put(
            id,
            label);

        // Increment associated counter
        result.incrementNewManagedObjectsRolledBackCount();

      } catch (RSuiteException e) {
        result.addWarning(
            rollbackLabel,
            new RSuiteException(RSuiteException.ERROR_INTERNAL_ERROR, OperationMessageProperties.get(
                    "rollback.warn.unable.to.process.new.asset",
                    label,
                    id,
                    e.getMessage()), e));
        continue;
      }
    }
  }

  /**
   * Rollback the provided map of assets by revert to the previous version.
   * 
   * @param context
   * @param user
   * @param result
   * @param assetsToProcess Map where the key is the MO ID to revert to the previous version, and
   *        the value is the label to use for the MO.
   * @param processedAssets Map populated by this method, identifying the MOs it was able to revert
   *        to the previous version. Same key-value use as the above map.
   */
  private static void rollbackUpdatedAssets(
      ExecutionContext context,
      User user,
      OperationResult result,
      Map<String, String> assetsToProcess,
      Map<String, String> processedAssets) {

    String rollbackLabel = OperationMessageProperties.get("rollback.label");

    // Bail if there are no assets to process.
    if (assetsToProcess == null || assetsToProcess.size() == 0) {
      result.addInfoMessage(
          rollbackLabel,
          OperationMessageProperties.get("rollback.info.no.updated.assets"));
      return;
    }

    result.addInfoMessage(
        rollbackLabel,
        OperationMessageProperties.get(
            "rollback.info.processing.updated.assets",
            assetsToProcess.size()));

    // Attempt to revert each MO given to us.
    ObjectRollbackOptions rollbackOptions = new ObjectRollbackOptions();
    for (Map.Entry<String, String> entry : assetsToProcess.entrySet()) {
      String id = entry.getKey();
      String label = entry.getValue();
      try {
        result.addInfoMessage(
            rollbackLabel,
            OperationMessageProperties.get(
                "rollback.info.processing.updated.asset",
                label,
                id));

        // Determine the version specifier for the previous version
        VersionSpecifier versionSpecifier = MOUtils.getPreviousVersionSpecifier(
            context,
            user,
            id);
        if (versionSpecifier == null) {
          result.addWarning(
              rollbackLabel,
              new RSuiteException(RSuiteException.ERROR_INTERNAL_ERROR, OperationMessageProperties.get(
                      "rollback.warn.updated.asset.has.one.version",
                      label,
                      id)));
          continue;
        }

        // Make sure the current user has or can check out the MO.
        MOUtils.checkout(
            context,
            user,
            id);

        // Attempt rollback.
        context.getManagedObjectService().rollback(
            user,
            versionSpecifier,
            rollbackOptions);

        // Add to processed list
        processedAssets.put(
            id,
            label);

        // Increment associated counter
        result.incrementUpdatedManagedObjectsRolledBackCount();

      } catch (RSuiteException e) {
        result.addWarning(
            rollbackLabel,
            new RSuiteException(RSuiteException.ERROR_INTERNAL_ERROR, OperationMessageProperties.get(
                    "rollback.warn.unable.to.process.updated.asset",
                    label,
                    id,
                    e.getMessage()), e));
        continue;
      }
    }
  }
}
