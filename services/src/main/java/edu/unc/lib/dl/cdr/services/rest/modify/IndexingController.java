package edu.unc.lib.dl.cdr.services.rest.modify;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.unc.lib.dl.acl.service.AccessControlService;
import edu.unc.lib.dl.acl.util.AccessGroupConstants;
import edu.unc.lib.dl.acl.util.AccessGroupSet;
import edu.unc.lib.dl.acl.util.GroupsThreadStore;
import edu.unc.lib.dl.cdr.services.processing.MessageDirector;
import edu.unc.lib.dl.data.ingest.solr.SolrUpdateRequest;
import edu.unc.lib.dl.util.IndexingActionType;

@Controller
public class IndexingController {
	private static final Logger log = LoggerFactory.getLogger(IndexingController.class);
	
	@Autowired
	private MessageDirector messageDirector;
	@Autowired
	private AccessControlService accessControlService;

	/**
	 * Perform a deep reindexing operation on the specified id and all of its children.
	 * 
	 * @param id
	 * @param inplace
	 * @return
	 */
	@RequestMapping(value = "edit/solr/reindex/{id}", method = RequestMethod.POST)
	public void reindex(@PathVariable("id") String id, @RequestParam(value = "inplace", required = false) Boolean inplace,
			HttpServletResponse response) {
		if (!hasPermission(id)) {
			response.setStatus(401);
			return;
		}

		if (inplace == null || inplace) {
			log.info("Reindexing " + id + ", inplace reindex mode");
			messageDirector.direct(new SolrUpdateRequest(id, IndexingActionType.RECURSIVE_REINDEX));
		} else {
			log.info("Reindexing " + id + ", clean reindex mode");
			messageDirector.direct(new SolrUpdateRequest(id, IndexingActionType.CLEAN_REINDEX));
		}
	}
	
	/**
	 * Perform a shallow reindexing of the object specified by id
	 * 
	 * @param id
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "edit/solr/update/{id}", method = RequestMethod.POST)
	public void reindex(@PathVariable("id") String id, HttpServletResponse response) {
		if (!hasPermission(id)) {
			response.setStatus(401);
			return;
		}

		log.info("Updating " + id);
		messageDirector.direct(new SolrUpdateRequest(id, IndexingActionType.ADD));
	}

	private boolean hasPermission(String id) {
		// Disallow requests by users that are not at least curators for pid
		AccessGroupSet groups = GroupsThreadStore.getGroups();
		if (log.isDebugEnabled()) {
			log.debug("hasPermission for groups " + groups.joinAccessGroups(";"));
		}
		return groups.contains(AccessGroupConstants.ADMIN_GROUP);
	}

	public void setMessageDirector(MessageDirector messageDirector) {
		this.messageDirector = messageDirector;
	}

	public void setAccessControlService(AccessControlService accessControlService) {
		this.accessControlService = accessControlService;
	}
}
