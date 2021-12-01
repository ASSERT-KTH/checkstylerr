package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DATA_OWNER;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_OWNER_STEM_ID;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_SERVICE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;
import static edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperObjectTypesConfiguration {
  
  /**
   * retrieve type setting for a given grouper object (group/stem) and object type name.
   * @param grouperObject
   * @param objectTypeName
   * @return
   */
  public static GrouperObjectTypesAttributeValue getGrouperObjectTypesAttributeValue(GrouperObject grouperObject, String objectTypeName) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, objectTypeName);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperObjectTypeAttributeValue(attributeAssign);
  }

  /**
   * retrieve all the configured type settings for a given grouper object (group/stem)
   * @param grouperObject
   * @return
   */
  public static List<GrouperObjectTypesAttributeValue> getGrouperObjectTypesAttributeValues(final GrouperObject grouperObject) {
    
    final List<GrouperObjectTypesAttributeValue> result = new ArrayList<GrouperObjectTypesAttributeValue>();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        for (String objectType: GrouperObjectTypesSettings.getObjectTypeNames()) {
          GrouperObjectTypesAttributeValue value = getGrouperObjectTypesAttributeValue(grouperObject, objectType);
          if (value != null) {
            result.add(value);
          }
        }
        
        return null;
      }
      
    });
    
    return result;
  }
  
  /**
   * save or update type config for a given grouper object (group/stem)
   * @param grouperObjectTypesAttributeValue
   * @param grouperObject
   */
  public static void saveOrUpdateTypeAttributes(GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue, GrouperObject grouperObject) {
    
    AttributeAssign attributeAssign = getAttributeAssign(grouperObject, grouperObjectTypesAttributeValue.getObjectTypeName());
   
    if (attributeAssign == null) {
      if (grouperObject instanceof Group) {
        attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else if (grouperObject instanceof Stem) {
        attributeAssign = ((Stem)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else {
        throw new RuntimeException("Only Groups and Folders can have types");
      }
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(grouperObjectTypesAttributeValue.isDirectAssignment()));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeName());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DATA_OWNER, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeDataOwner());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeMemberDescription());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_SERVICE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.getObjectTypeServiceName());
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_OWNER_STEM_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperObjectTypesAttributeValue.isDirectAssignment() ? null: grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    
    attributeAssign.saveOrUpdate();
    
    if (grouperObject instanceof Stem && grouperObjectTypesAttributeValue.isDirectAssignment()) {
      GrouperObjectTypesAttributeValue valueToSave = GrouperObjectTypesAttributeValue.copy(grouperObjectTypesAttributeValue);
      valueToSave.setObjectTypeOwnerStemId(((Stem)grouperObject).getId());
      valueToSave.setDirectAssignment(false);
      saveOrUpdateTypeAttributesOnChildren((Stem)grouperObject, valueToSave);
    }
    
  }
  
  /**
   * find type config in the parent hierarchy for a given grouper object for all object types (ref, basis, etc) and assign that config to this grouper object.
   * @param grouperObject
   */
  public static void copyConfigFromParent(final GrouperObject grouperObject) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        for (String objectType: GrouperObjectTypesSettings.getObjectTypeNames()) {
          copyConfigFromParent(grouperObject, objectType);
        }
        
        return null;
        
      }
      
    });
    
  }
  
  /**
   * find type config in the parent hierarchy for a given grouper object and type. Assign that config to the given grouper object
   * @param grouperObject
   * @param objectType
   */
  public static void copyConfigFromParent(GrouperObject grouperObject, String objectType) {
    
    //don't do this now
    if (GrouperCheckConfig.isInCheckConfig() || !GrouperObjectTypesSettings.objectTypesEnabled()) {
      return;
    }
    
    if (grouperObject instanceof Stem && ((Stem) grouperObject).isRootStem()) {
      return;
    }
    
    deleteAttributeAssign(grouperObject, objectType);
    
    // if we changed from direct to indirect, we need to go through all the children
    // and delete metadata on them that were inheriting from this stem.
    if (grouperObject instanceof Stem) {
      deleteAttributesOnAllChildrenWithIndirectConfig((Stem)grouperObject, objectType);
    }
    
    Stem parent = grouperObject.getParentStem();
    
    if(parent.isRootStem()) {
      return;
    }
    
    GrouperObjectTypesAttributeValue savedValue = null;
    
    while (parent != null) {
      
      GrouperObjectTypesAttributeValue attributeValue = getGrouperObjectTypesAttributeValue(parent, objectType);
      
      if (attributeValue != null && attributeValue.isDirectAssignment()) {
        savedValue = new GrouperObjectTypesAttributeValue();
        savedValue.setDirectAssignment(false);
        savedValue.setObjectTypeDataOwner(attributeValue.getObjectTypeDataOwner());
        savedValue.setObjectTypeMemberDescription(attributeValue.getObjectTypeMemberDescription());
        savedValue.setObjectTypeName(attributeValue.getObjectTypeName());
        savedValue.setObjectTypeOwnerStemId(parent.getId());
        savedValue.setObjectTypeServiceName(attributeValue.getObjectTypeServiceName());
        saveOrUpdateTypeAttributes(savedValue, grouperObject);
        break;
      }
      
      parent = parent.getParentStem();
      
      if (parent.isRootStem()) {
        break;
      }
      
    }
    
    // if it's a stem where we changed from direct to indirect, we need to go through all the children of that stem and update the attributes
    //with parent's metadata
    if (grouperObject instanceof Stem && savedValue != null) {
      saveOrUpdateTypeAttributesOnChildren((Stem)grouperObject, savedValue);
    }
    
  }
  
  /**
   * find all stems where given subject is admin of service
   * @return
   */
  public static List<Stem> findStemsWhereCurrentUserIsAdminOfService(Subject subject) {
    
    List stems = new ArrayList<Stem>(new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignSubject(subject)
        .assignNameOfAttributeDefName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT).addAttributeValuesOnAssignment("true")
        .assignNameOfAttributeDefName2(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME).addAttributeValuesOnAssignment2("service")
        .addPrivilege(NamingPrivilege.STEM_ADMIN).findStems());
    
    return stems;
  }
  
  /**
   * search all the children of the given stem and return stems with object types
   *  that are candidates for auto assigning types
   * @param stem
   * @param children
   * @return
   */
  public static List<StemObjectType> getAutoAssignTypeStemCandidates(Stem stem, Set<Stem> children) {
    
    List<StemObjectType> result = new ArrayList<StemObjectType>();
    
    for (Stem child: children) {
      
      String folderExtension = child.getExtension().toLowerCase();
      String objecType = GrouperObjectTypesSettings.getFolderExtensionToTypeSuggestion().get(folderExtension);
      if (StringUtils.isNotBlank(objecType)) {
        // if folder already has types assigned, no need to suggest anything
        List<GrouperObjectTypesAttributeValue> objectTypesAttributeValues = getGrouperObjectTypesAttributeValues(child);
        if (objectTypesAttributeValues.size() == 0) {
          result.add(new StemObjectType(child, objecType));
        }
      }
      
    }
    
    return result;
  }
  
  private static void deleteAttributesOnAllChildrenWithIndirectConfig(Stem stem, String objectType) {
    
    Set<GrouperObject> children = new HashSet<GrouperObject>(stem.getChildGroups(Scope.SUB));
    children.addAll(stem.getChildStems(Scope.SUB));
    
    for (GrouperObject childGrouperObject: children) {
      GrouperObjectTypesAttributeValue mayBeGroupTypeAttributeValue = getGrouperObjectTypesAttributeValue(childGrouperObject, objectType);
      if (mayBeGroupTypeAttributeValue != null) {
        
        if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
          continue;
        }
        
        String ownerStemId = mayBeGroupTypeAttributeValue.getObjectTypeOwnerStemId();
        if (stem.getId().equals(ownerStemId)) {
          deleteAttributeAssign(childGrouperObject, objectType);
        }
      }
      
    }
    
  }

  private static void deleteAttributeAssign(GrouperObject grouperObject, String objectType) {
    AttributeAssign currentAttributeAssign = getAttributeAssign(grouperObject, objectType);
    if (currentAttributeAssign != null) {
      currentAttributeAssign.delete();
    }
  }
  
  private static void saveOrUpdateTypeAttributesOnChildren(Stem parentStem, GrouperObjectTypesAttributeValue valueToSave) {
    
    Set<String> childrenStemIds = new HashSet<String>();
    
    for (Stem stem: parentStem.getChildStems(Scope.SUB)) {
      childrenStemIds.add(stem.getId());
    }
    
    Set<GrouperObject> children = new HashSet<GrouperObject>(parentStem.getChildGroups(Scope.SUB));
    children.addAll(parentStem.getChildStems(Scope.SUB));
    
    for (GrouperObject childGrouperObject: children) {
      boolean shouldSaveForThisChild = true;
      
      GrouperObjectTypesAttributeValue mayBeGroupTypeAttributeValue = getGrouperObjectTypesAttributeValue(childGrouperObject, valueToSave.getObjectTypeName());
      if (mayBeGroupTypeAttributeValue != null) {
        
        if (mayBeGroupTypeAttributeValue.isDirectAssignment()) {
          shouldSaveForThisChild = false;
          continue;
        }
        
        String ownerStemId = mayBeGroupTypeAttributeValue.getObjectTypeOwnerStemId();

        // some child of parentStem's settings are already configured on this group/stem, we don't need to update because we will increase the distance otherwise
        if (childrenStemIds.contains(ownerStemId)) {
          shouldSaveForThisChild = false;
        }
        
      }
      
      if (shouldSaveForThisChild) {
        saveOrUpdateTypeAttributes(valueToSave, childGrouperObject);
      }
      
    }
    
  }
  
  
  private static AttributeAssign getAttributeAssign(GrouperObject grouperObject, String objectType) {
    
    Set<AttributeAssign> attributeAssigns = new HashSet<AttributeAssign>();
    
    if (grouperObject instanceof Group) {
      Group group = (Group)grouperObject;
      attributeAssigns = group.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    } else if (grouperObject instanceof Stem) {
      Stem stem = (Stem)grouperObject;
      attributeAssigns = stem.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    }
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      }
      
      String objectTypeNameFromDB = attributeAssignValue.getValueString();
      if (objectType.equals(objectTypeNameFromDB)) {
       return attributeAssign;
      }
    }
    return null;
    
  } 
  
  private static GrouperObjectTypesAttributeValue buildGrouperObjectTypeAttributeValue(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperObjectTypesAttributeValue result = new GrouperObjectTypesAttributeValue();
    result.setObjectTypeName(attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME).getValueString());
    
    AttributeAssignValue dataOwnerAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DATA_OWNER);
    result.setObjectTypeDataOwner(dataOwnerAssignValue != null ? dataOwnerAssignValue.getValueString(): null);
    
    AttributeAssignValue memberDescriptionAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_MEMBERS_DESCRIPTION);
    result.setObjectTypeMemberDescription(memberDescriptionAssignValue != null ? memberDescriptionAssignValue.getValueString(): null);

    AttributeAssignValue serviceNameAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_SERVICE_NAME);
    result.setObjectTypeServiceName(serviceNameAssignValue != null ? serviceNameAssignValue.getValueString(): null);
    
    AttributeAssignValue ownerStemIdAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_OWNER_STEM_ID);
    result.setObjectTypeOwnerStemId(ownerStemIdAssignValue != null ? ownerStemIdAssignValue.getValueString(): null);
    
    AttributeAssignValue directAssignmentAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT);
    String directAssignmentStr = directAssignmentAssignValue != null ? directAssignmentAssignValue.getValueString(): null;
    boolean directAssignment = BooleanUtils.toBoolean(directAssignmentStr);
    result.setDirectAssignment(directAssignment);
    return result;
  }

}
